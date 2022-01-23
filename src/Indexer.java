import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
    Map<String, List<Tuple>> Index;
    List<String> Files;

    Indexer(Map<String, List<Tuple>> In, List<String> Fi) {
        this.Index = In;
        this.Files = Fi;
    }

    public void indexFile(String FilePath) throws IOException {
        File File = new File(FilePath);
        int FileNum = Files.indexOf(File.getPath());
        if (FileNum == -1) {
            Files.add(File.getPath());
            FileNum = Files.size() - 1;
        }

        int Position = 0;
        BufferedReader Reader = new BufferedReader(new FileReader(File));
        for (String Line = Reader.readLine(); Line != null; Line = Reader.readLine()) {
            for (String word : Line.split("\\W+")) {
                String lowWord = word.toLowerCase();
                Position++;
                List<Tuple> Idx = Index.get(lowWord);
                if (Idx == null) {
                    Idx = new LinkedList<>();
                    Index.put(lowWord, Idx);
                }
                Idx.add(new Tuple(FileNum, Position));
            }
        }
//        System.out.println("indexed " + File.getPath() + " " + Position + " words");
    }


    public static String search(List<String> words, Map<String, List<Tuple>> Index, List<String> Files) {
        String Result = "";

        for (String word : words) {
            String SubResult = "\n";
            Set<String> answer = new HashSet<>();
            String lowWord = word.replaceAll("[^a-zA-Z0-9 ]", "")
                    .trim()
                    .replaceAll(" +", " ")
                    .toLowerCase();

            List<Tuple> idx = Index.get(lowWord);
            try {
                if (idx != null) {
                    for (Tuple t : idx) {
                        answer.add(Files.get(t.fileno));
                    }
                }
                SubResult += lowWord + ": ";
                for (String f : answer) {
                    SubResult += f + " ";
                }
            } catch (Exception e) {
                continue;
            }
            Result += "\n" + SubResult;
        }
        return Result;
    }


    public static Indexer main() {
        Map<String, List<Tuple>> index = new ConcurrentHashMap<>();
        List<String> files = new ArrayList<>();
        Indexer Index = new Indexer(index, files);
        int ProcessNum = 8;

        try {
            String path = new File("aclImdb").getAbsolutePath();
            File Dir = new File(path);
            File[] DirList = Dir.listFiles();
            ArrayList AllFileList = new ArrayList();

            assert DirList != null;
            for (File Folder : DirList) {
                if (!(Folder.getAbsolutePath()).contains(".DS_Store")) {
                    File[] SubDirList = Folder.listFiles();
                    assert SubDirList != null;

                    for (File SubFolder : SubDirList) {
                        if (!(SubFolder.getAbsolutePath()).contains(".DS_Store")) {
                            AllFileList.add(SubFolder.getAbsoluteFile());
                        }
                    }
                }
            }

            ArrayList<Worker> Workers = new ArrayList<>();
            for (int i = 0; i < ProcessNum; i++) {
                ArrayList<String> FileList = new ArrayList<>();
                for (int m = 0; (i + m) < AllFileList.size(); m += ProcessNum) {
                    FileList.add(AllFileList.get(i + m).toString());
                }
                Worker Work = new Worker(i, FileList, Index.Index, Index.Files);
                Workers.add(Work);
            }
            for (int i = 0; i < ProcessNum; i++) {
                Workers.get(i).start();
            }
            for (int i = 0; i < ProcessNum; i++) {
                Workers.get(i).join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(Index);
        return Index;
    }


    public static class Worker extends Thread {
        int Number;
        Map<String, List<Tuple>> index;
        ArrayList<String> PersonalList;
        List<String> files;

        Worker(int i, ArrayList<String> List, Map<String, List<Tuple>> in, List<String> f) {
            this.Number = i;
            this.index = in;
            this.PersonalList = List;
            this.files = f;
        }

        public void run() {
            Indexer idx = new Indexer(index, files);

            for (int i = 0; i < PersonalList.size(); i++) {
                try {
                    idx.indexFile(PersonalList.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class Tuple {
        public int fileno;
        private int position;

        public Tuple(int fileno, int position) {
            this.fileno = fileno;
            this.position = position;
        }
    }

}
