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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class Histogram {

    public static void main(String[] args) {


        final Configurator<Integer, Integer, Integer, Integer> config = new Configurator<>();


        class Spliter extends SpliterAlgorithmFile {

            private boolean closed = false;
            private final int chunksize = 249_999;
            private byte[] chunk = new byte[chunksize];
            private int counter1 = 0;
            private int counter2 = 0;
            private int width;
            private int height;
            private BufferedImage image;

            public Spliter(File file) {
                super(file);
                try {
                    image = ImageIO.read(file);
                    width = image.getWidth();
                    height = image.getHeight();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public DataWrapper nextChunk(){

                if (closed) {
                    return null;
                }

                DataWrapper data = new DataWrapper();



                int k = 0;
                int rgb;
                Color color;
                int red;
                int green;
                int blue;

                int totpix = width + height;
                
                for (int i = counter1; i < width; i++) {
                    for (int j = counter2; j < height; j++) {
                        if (k >= chunksize) {
                            counter1 = i;
                            counter2 = j;
                            
                            data.setProgressPercentage((int)(((float)(i + j) / (float)totpix) * 100));
                            
                            data.wrapByteArray(chunk);

                            return data;
                        }

                        rgb = image.getRGB(i, j);

                        color = new Color(rgb);
                        red = color.getRed();
                        green = color.getGreen();
                        blue = color.getBlue();
                        chunk[k] = pixelToByte(red);
                        chunk[k + 1] = pixelToByte(green);
                        chunk[k + 2] = pixelToByte(blue);

                        k += 3;

                    }
                    counter2 = 0;
                }
                closed = true;
//                image = null;
                if (k > 0) {
                    byte[] tmp = Arrays.copyOfRange(chunk, 0, k);
                    data.wrapByteArray(tmp);
                    return data;
                } else {

                    return null;
                }

            }

            @Override
            public void reset() {
                closed = false;
                counter1 = 0;
                counter2 = 0;
                chunk = new byte[chunksize];
            }
        }
        //define mapper		
        Mapper<Integer, Integer> mapper = new Mapper<Integer, Integer>() {

            @Override
            public void map(DataWrapper data, DataCollector<Integer, Integer> collector) {

                byte[] dataUnwrap = data.unwrapByteArray();

                int size = dataUnwrap.length;
                int red;
                int green;
                int blue;
                for (int i = 0; i < size; i = i + 3) {
                    red = byteToPixel(dataUnwrap[i]);
                    green = byteToPixel(dataUnwrap[i + 1]);
                    blue = byteToPixel(dataUnwrap[i + 2]);

                    collector.collect(blue, 1); //blue
                    collector.collect(green + 256, 1); //green
                    collector.collect(red + 512, 1); //red
                }

            }
        };


        //define reducer
        Reducer<Integer, Integer, Integer, Integer> reducer = new Reducer<Integer, Integer, Integer, Integer>() {

            @Override
            public void reduce(Integer key, ArrayList<Integer> valueList, DataCollector<Integer, Integer> collector) {

                int sum = 0;
                for (int value : valueList) {
                    sum += value;
                }

                collector.collect(key, sum);
            }
        };


        class Combiner implements CombinerAlgorithm<Integer, Integer> {

            @Override
            public Integer combine(Integer value1, Integer value2) {
                return value1 + value2;
            }

            @Override
            public void reset() {
            }
        }

        TaskDescription mapTask = new TaskDescription("map1", TaskCategory.MAP);
        TaskDescription reduceTask = new TaskDescription("reduce1", TaskCategory.REDUCE);

        if (args.length == 2) {
            mapTask.setSpliter(new Spliter(new File(args[1])));
        } else {
            mapTask.setSpliter(new Spliter(new File("small.bmp")));
        }
        mapTask.setCombiner(new Combiner());
        mapTask.setMapper(mapper);

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
        mapTask.setSizeForFixedSizeArrayCollector(768);
        mapTask.setDataCollector(3);

        //start task

        boolean feedback;
        MapReduce<Integer, Integer, Integer, Integer> mapreduce = new MapReduce<>();
        feedback = mapreduce.initialize(config);
        if (feedback) {
            mapreduce.start();
        } else {
            System.out.println("Feedback false.");
            //end program
        }
    }

    public static byte pixelToByte(int pixel) {
        return (byte) (pixel - 128);
    }

    public static int byteToPixel(byte pixelByte) {
        return ((int) (pixelByte) + 128);
    }
}
