package epfl.project;

/**
 * JarEntryPoint.java (UTF-8)
 *
 * 4 mai 2012
 *
 * @author Loic
 */
public class JarEntryPoint {

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            PCA.main(args);
        } else {
            switch (args[0].toLowerCase()) {
                case "dummy":
                    Test.main(args);
                    break;
                case "histogram":
                    Histogram.main(args);
                    break;
                case "pca":
                    PCA.main(args);
                    break;
                case "stringmatch":
                    StringMatch.main(args);
                    break;
                case "wordcount":
                    WordCount.main(args);
                    break;
                case "charcount":
                    CharCount.main(args);
                    break;
                case "tccharcount":
                    epfl.project.threadpoolcomparison.CharCount.main(args);
                    break;
                case "tcencryptionstringmatch":
                    epfl.project.threadpoolcomparison.CharCountForkJoin.main(args);
                    break;
                case "tcfibonnacitest":
                    epfl.project.threadpoolcomparison.FibonnaciTest.main(args);
                    break;
                case "tcfilesort":
                    epfl.project.threadpoolcomparison.FileSort.main(args);
                    break;
                case "tchistogram":
                    epfl.project.threadpoolcomparison.Histogram.main(args);
                    break;
                case "tccharcountforkjoin":
                    epfl.project.threadpoolcomparison.CharCountForkJoin.main(args);
                    break;
                case "tcpca":
                    epfl.project.threadpoolcomparison.PCA.main(args);
                    break;
                case "tcwordcount":
                    epfl.project.threadpoolcomparison.WordCount.main(args);
                    break;
                case "tcwordcount2":
                    epfl.project.threadpoolcomparison.WordCount2.main(args);
                    break;
                case "tcwordcountforkjoin":
                    epfl.project.threadpoolcomparison.WordCountForkJoin.main(args);
                    break;
                case "monitor":
                    epfl.monitor.gui.MainGui.main(args);
                    break;
                default:
                    System.err.println("wrong entry point");
            }
        }
    }
}
