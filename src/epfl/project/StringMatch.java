package epfl.project;

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

public class StringMatch {

    public static void main(String[] args) {

        final Configurator<String, Integer, Integer, Integer> config = new Configurator<>();

        final ArrayList<String> stringMatch = new ArrayList<>();// encryptArray(stringMatchTmp);
        readEncryption(stringMatch);

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
                    bufferR = new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        Mapper<String, Integer> mapper = new Mapper<String, Integer>() {

            @Override
            public void map(DataWrapper data, DataCollector<String, Integer> collector) {

                String strData = data.unwrapString();
                if (strData.length() > 0 && stringMatch != null && !stringMatch.isEmpty()) {
                    String[] words = strData.split("\\s");
                    for (String string : words) {
                        for (String match : stringMatch) {
                            if (match.equals(encrypt(string))) {
                                System.out.println("FOUND: " + match + " IS " + string);
                                break;
                            }
                        }
                    }

                }
            }
        };


        //define reducer
        Reducer<String, Integer, Integer, Integer> reducer = new Reducer<String, Integer, Integer, Integer>() {

            @Override
            public void reduce(String key, ArrayList<Integer> valueList,
                    DataCollector<Integer, Integer> collector) {
                //nothing
            }
        };


        TaskDescription mapTask = new TaskDescription("map1", TaskCategory.MAP);
        TaskDescription reduceTask = new TaskDescription("reduce1", TaskCategory.REDUCE);

        mapTask.setMapper(mapper);
        
        if (args.length == 2) {
            mapTask.setSpliter(new Spliter(new File(args[1])));
        } else {
            mapTask.setSpliter(new Spliter(new File("stringmatch.txt")));
        }
        

        reduceTask.setReducer(reducer);

        config.addTask(0, mapTask);
        config.addTask(1, reduceTask);


        try {
            reduceTask.setOutput(new File("res.txt"));
        } catch (TaskException ex) {
            System.out.println(ex.getMessage());
        }

        config.setConsoleActive(true);
        config.setGUIActive(false);



        //start task

        boolean feedback;
        MapReduce<String, Integer, Integer, Integer> mapreduce = new MapReduce<>();
        feedback = mapreduce.initialize(config);
        if (feedback) {
            mapreduce.start();
        } else {
            System.out.println("Feedback false.");
        }
    }

    public static String encrypt(String word) {
        String encryptedWord = "";
        for (int i = 0; i < word.length(); i++) {
            encryptedWord += (char) (((int) word.charAt(i)) + 1);
        }
        return encryptedWord;
    }

    
    public static void readEncryption(ArrayList<String> encrypted){
		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader("encryption.txt"));
			String text = "";
			while ((text = r.readLine()) != null) {
				encrypted.add(text);
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
