package epfl.project;

import epfl.project.algorithm.CombinerAlgorithm;
import epfl.project.algorithm.SpliterAlgorithmFile;
import epfl.project.common.Configurator;
import epfl.project.common.DataCollector;
import epfl.project.common.DataWrapper;
import epfl.project.common.MapReduce;
import epfl.project.mapreduce.Mapper;
import epfl.project.mapreduce.Reducer;
import epfl.project.scheduler.TaskCategory;
import epfl.project.scheduler.TaskDescription;
import epfl.project.scheduler.TaskException;
import java.io.*;
import java.util.ArrayList;

public class Test {

    public Test() {
    }

    public void start(int index) {
        final Configurator<String, Integer, String, Integer> config = new Configurator<>();

        Mapper<String, Integer> mapper = new Mapper<String, Integer>() {

            @Override
            public void map(DataWrapper data, DataCollector<String, Integer> collector) {

                String strData = data.unwrapString();

                if (strData.length() > 0) {
                    String[] words = strData.split("\\W");

                    for (String string : words) {
                        if (!string.isEmpty()) {
                            collector.collect(string, 1);
                        }
                    }
                }

            }
        };
        Reducer<String, Integer, String, Integer> reducer = new Reducer<String, Integer, String, Integer>() {

            @Override
            public void reduce(String key, ArrayList<Integer> valueList, DataCollector<String, Integer> collector) {
                int sum = 0;
                for (int value : valueList) {
                    sum += value;
                }
                collector.collect(key, sum);
            }
        };
        TaskDescription mapTask = new TaskDescription("map1", TaskCategory.MAP);
        TaskDescription reduceTask = new TaskDescription("reduce1", TaskCategory.REDUCE);

        mapTask.setSpliter(new Spliter(new File("word_100MB.txt")));
        mapTask.setCombiner(new Combiner());
        mapTask.setMapper(mapper);

        // mapTask.enableRedundantErrorDetector(5, 50);


        reduceTask.setReducer(reducer);

        //reduceTask.enableRedundantErrorDetector(5, 50);

        config.addTask(0, mapTask);
        config.addTask(1, reduceTask);


        try {
            reduceTask.setOutput(new File("res.txt"));
        } catch (TaskException ex) {
            System.out.println(ex.getMessage());
        }

        config.setConsoleActive(false);
        config.setGUIActive(false);
        mapTask.setDataCollector(2);


        boolean feedback;
        MapReduce<String, Integer, String, Integer> mapreduce = new MapReduce<>();
        feedback = mapreduce.initialize(config);
        if (feedback) {
            //******
            mapreduce.start("redef partition " + index, "save b");
            //*********
        } else {
            System.out.println("Feedback false.");
            //end program
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Object o = new Object();
        int[] tests = new int[]{1, 2};
        for (int i : tests) {
            synchronized (o) {
                o.wait(2000);
                Test t = new Test();
                t.start(i);
                System.err.println("****************************************");
                t = null;
            }
        }
    }

    class Combiner implements CombinerAlgorithm<String, Integer> {

        @Override
        public Integer combine(Integer value1, Integer value2) {

            return value1 + value2;
        }

        @Override
        public void reset() {
        }
    }

    class Spliter extends SpliterAlgorithmFile {

        private boolean closed = false;
        BufferedReader bufferR;

        public Spliter(File file) {
            super(file);
            try {
                bufferR = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public DataWrapper nextChunk() throws IOException {

            if (closed) {
                return null;
            }

            DataWrapper data = new DataWrapper();
            int numberLineRead = 5000;
            StringBuilder text = new StringBuilder();
            String line;
            for (int i = 0; i < numberLineRead && !closed; i++) {
                line = bufferR.readLine();
                if (line == null) {
                    closed = true;
                    bufferR.close();
                    data.wrapString(text.toString());
                    return data;
                }
                text.append(line).append(" ");

            }
            data.wrapString(text.toString());
            return data;

        }

        @Override
        public void reset() {
            closed = false;
            try {
                try {
                    bufferR.close();
                } catch (IOException ex) {
                }
                bufferR = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
