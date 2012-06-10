package epfl.project;

import epfl.project.algorithm.SpliterAlgorithmFile;
import epfl.project.algorithm.SpliterAlgorithmReduce;
import epfl.project.common.*;
import epfl.project.mapreduce.Mapper;
import epfl.project.mapreduce.Reducer;
import epfl.project.scheduler.TaskCategory;
import epfl.project.scheduler.TaskDescription;
import epfl.project.scheduler.TaskException;
import java.io.*;
import java.util.ArrayList;

public class PCA {

    static int ROW = 1000;
    static int COLUMN = 1000;
    static double[][] matrix = new double[1000][1000];

    public static void main(String[] args) {
        Configurator<Integer, Double, Integer, Double> config = new Configurator<>();

        final class Spliter1 extends SpliterAlgorithmFile {

            private boolean closed = false;
            int numRowForMapper = 0;

            public Spliter1(File file) {
                super(file);
                readMatrix();
            }

            //each mapper will have one row so it return the number of the row that the mapper will user
            @Override
            public DataWrapper nextChunk() throws IOException {

                if (closed) {
                    return null;
                }

                DataWrapper data = new DataWrapper();
                if (numRowForMapper < ROW) {
                    data.wrapInt(numRowForMapper);
                    numRowForMapper++;
                    return data;
                } else {
                    closed = true;
                    return null;
                }

            }

            @Override
            public void reset() {
                closed = false;
                readMatrix();
                numRowForMapper = 0;
            }

            public void readMatrix() {
                try {
                    //added try-catch with resources (new in java 7)
                    //http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
                    try (BufferedReader b = new BufferedReader(new FileReader(file))) {
                        String line = "";
                        String[] array;
                        int i = 0;
                        int sizeArray;
                        while ((line = b.readLine()) != null) {
                            array = line.split(" ");
                            sizeArray = array.length;
                            for (int j = 0; j < sizeArray; j++) {
                                matrix[i][j] = Double.valueOf(array[j]);
                            }
                            i++;
                        }
                        b.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //define mapper

        // compute the mean (first map)
        //http://en.wikipedia.org/wiki/Principal_component_analysis
        Mapper<Integer, Double> mapper1 = new Mapper<Integer, Double>() {

            @Override
            public void map(DataWrapper data, DataCollector<Integer, Double> collector) {

                int rowNumber = data.unwrapInt();
                double mean = 0.0;
                for (int i = 0; i < COLUMN; i++) {
                    mean += matrix[rowNumber][i];
                }
                mean = mean / COLUMN;
                collector.collect(rowNumber, mean);
            }
        };

        //define reducer
        Reducer<Integer, Double, Integer, Double> reducer = new Reducer<Integer, Double, Integer, Double>() {

            @Override
            public void reduce(Integer key, ArrayList<Double> valueList, DataCollector<Integer, Double> collector) {
                collector.collect(key, valueList.get(0));
            }
        };

        // first step of PCA
        TaskDescription mapTask = new TaskDescription("map1", TaskCategory.MAP);
        TaskDescription reduceTask = new TaskDescription("reduce1", TaskCategory.REDUCE);
        if (args.length == 4) {
            ROW = Integer.parseInt(args[2]);
            COLUMN = Integer.parseInt(args[3]);
            matrix = new double[ROW][COLUMN];
            mapTask.setSpliter(new Spliter1(new File(args[1])));
        } else {
            mapTask.setSpliter(new Spliter1(new File("PCA1000x1000")));
        }
        mapTask.setMapper(mapper1);

        reduceTask.setReducer(reducer);

        config.addTask(0, mapTask);
        config.addTask(1, reduceTask);

        config.setConsoleActive(true);
        config.setGUIActive(true);
        config.disableWriteToDiskIfneeded();
        mapTask.setSizeForFixedSizeArrayCollector(ROW);
        mapTask.setDataCollector(4);

        try {
            reduceTask.setOutput(new File("res.txt"));
        } catch (TaskException e) {
            e.printStackTrace();
        }
        //////////////////



        // return two number or row associated with the 2 value
        final class Spliter2 extends SpliterAlgorithmReduce<Integer, Double> {

            private boolean closed = false;
            int counterRow1 = 0;
            int counterRow2 = 0;
            double[] mean;

            @Override
            public void reset() {
                closed = false;
                counterRow1 = 0;
                counterRow2 = 0;
                mean = null;
            }

            @Override
            public DataWrapper nextChunk() throws IOException {

                if (closed) {
                    return null;
                }

                // useful to understand which value we compute for the covariance (only the diagonal)
                if (mean == null) {
                    mean = new double[ROW];
                    do {
                        for (KeyValue<Integer, Double> tuple : lastReduceResult) {
                            mean[tuple.getKey()] = tuple.getValue();
                        }
                        this.nextReduceResult();
                    } while (lastReduceResult != null);

                }

                DataWrapper data = new DataWrapper();
                if (counterRow1 < ROW && counterRow2 < ROW) {

                    double[] dataInfo = {counterRow1, mean[counterRow1], counterRow2, mean[counterRow2]};
                    data.wrapDoubleArray(dataInfo);

                    counterRow2++;
                    if (counterRow2 == ROW) {
                        counterRow1++;
                        counterRow2 = counterRow1;
                    }
                    return data;
                } else {
                    closed = true;
                    return null;
                }
            }
        }



        // compute the covariance matrix
        //http://en.wikipedia.org/wiki/Principal_component_analysis
        //http://www.itl.nist.gov/div898/handbook/pmc/section5/pmc541.htm   but transposed
        Mapper<Integer, Double> mapper2 = new Mapper<Integer, Double>() {

            @Override
            public void map(DataWrapper data, DataCollector<Integer, Double> collector) {

                double[] info = data.unwrapDoubleArray();
                int numRow1ForMapper = (int) info[0];
                double mean1 = info[1];
                int numRow2ForMapper = (int) info[2];
                double mean2 = info[3];

                double cov = 0;
                for (int i = 0; i < COLUMN; i++) {
                    cov += (matrix[numRow1ForMapper][i] - mean1) * (matrix[numRow2ForMapper][i] - mean2);
                }
                cov = cov / (double) (COLUMN - 1);

                collector.collect(numRow1ForMapper * ROW + numRow2ForMapper, cov);
            }
        };


        TaskDescription mapTask2 = new TaskDescription("map2", TaskCategory.MAP);
        TaskDescription reduceTask2 = new TaskDescription("reduce2", TaskCategory.REDUCE);

        // second step of PCA	
        mapTask2.setSpliter(new Spliter2());
        mapTask2.setMapper(mapper2);

        reduceTask2.setReducer(reducer);

        mapTask2.setSizeForFixedSizeArrayCollector(ROW * ROW);
        mapTask2.setDataCollector(4);

        config.addTask(2, mapTask2);
        config.addTask(3, reduceTask2);
        try {
            reduceTask2.setOutput(new File("res.txt"));
        } catch (TaskException e) {
            e.printStackTrace();
        }
        ////////////////



        //start task

        boolean feedback;
        MapReduce<Integer, Double, Integer, Double> mapreduce = new MapReduce<>();
        feedback = mapreduce.initialize(config);
        if (feedback) {
            mapreduce.start();
        } else {
            System.out.println("Feedback false.");
            return;
            //end program
        }

    }
}
