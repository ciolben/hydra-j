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
import java.util.Arrays;

public class CharCount {

    public static void main(String[] args) {

        final Configurator<Character, Integer, Character, Integer> config = new Configurator<>();

        //define mapper


        //define two tasks for test

        TaskDescription mapTask = new TaskDescription("map1", TaskCategory.MAP);
        TaskDescription reduceTask = new TaskDescription("reduce1", TaskCategory.REDUCE);

        if (args.length == 2 || args.length == 4) {
            fillMapTask1(mapTask, args[1]);
        } else {
            fillMapTask1(mapTask, "word_100MB.txt");
        }
        
        fillReduceTask1(reduceTask);

        config.addTask(0, mapTask);
        config.addTask(1, reduceTask);

        config.nameRound(0, "a map round");
        config.nameRound(1, "the last round");
        
        config.setConsoleActive(args.length >= 3 ? false : true);
        config.setGUIActive(false);
        mapTask.setDataCollector(0);


        //start task

        boolean feedback;
        MapReduce<Character, Integer, Character, Integer> mapreduce = new MapReduce<>();
        feedback = mapreduce.initialize(config);

        if (feedback) {
            if (args.length >= 3) {
                boolean eq3 = args.length == 3;
                mapreduce.start("redef partition " + args[eq3?1:2], "save " + args[eq3?2:3]);
            } else {
                mapreduce.start(); 
            }
        } else {
            System.out.println("Feedback false.");
            //end program
        }
    }

    private static void fillMapTask1(TaskDescription mapTask, String filename) {
        
        class Spliter extends SpliterAlgorithmFile {
            private long length;
           private long pos;
            private boolean closed = false;
            private boolean isInit = false;
            BufferedReader bufferR;
            
            char[] charRead;

            public Spliter(File file) {
                super(file);
                charRead = new char[250000];
            }

            @Override
            public DataWrapper nextChunk() throws IOException {
                if (!isInit) {
                    try {
                        bufferR = new BufferedReader(new FileReader(file));
                        length = file.length();
                        pos = 0;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    isInit = true;
                }
                
                if (closed) {
                    return null;
                }

                DataWrapper data = new DataWrapper();

                int read = bufferR.read(charRead);
                if (read != -1) {
                    pos += read;
                    float perc = ((float)pos / (float)length) * 100.0f;
                    data.setProgressPercentage(Math.round(perc));
                    data.wrapCharArray(Arrays.copyOfRange(charRead, 0, read));
                } else {
                    closed = true;
                    bufferR.close();
                    return null;
                }
                return data;

            }

            @Override
            public void reset() {
                closed = false;
                isInit = false;
            }
        }
        class Combiner implements CombinerAlgorithm<Character, Integer> {

            @Override
            public Integer combine(Integer value1, Integer value2) {
                return value1 + value2;
            }

            @Override
            public void reset() {
            }
        };

        Mapper<Character, Integer> mapper = new Mapper<Character, Integer>() {

            @Override
            public void map(DataWrapper data, DataCollector<Character, Integer> collector) {
                char[] strData = data.unwrapCharArray();

                int size = strData.length;
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        collector.collect(strData[i], 1);
                    }
                }
            }
        };
        
        mapTask.setSpliter(new Spliter(new File(filename)));
        mapTask.setCombiner(new Combiner());
        mapTask.setMapper(mapper);
    }

    private static void fillReduceTask1(TaskDescription reduceTask) {
        //define reducer
        Reducer<Character, Integer, Character, Integer> reducer = new Reducer<Character, Integer, Character, Integer>() {

            @Override
            public void reduce(Character key, ArrayList<Integer> valueList, DataCollector<Character, Integer> collector) {
                int sum = 0;
                for (int value : valueList) {
                    sum += value;
                }
                collector.collect(key, sum);
            }
        };

        reduceTask.setReducer(reducer);
        try {
            reduceTask.setOutput(new File("res.txt"));
        } catch (TaskException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
