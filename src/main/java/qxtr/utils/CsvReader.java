package qxtr.utils;


import com.sun.istack.NotNull;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvReader implements Iterable<CsvReader.Line>, Iterator<CsvReader.Line> {

    @Override
    public Iterator<Line> iterator() {
        return this;
    }

    private Line next=null;

    @SneakyThrows
    @Override
    public boolean hasNext() {
        if (next!=null) return true;
        fetchNext();
        return next!=null;
    }

    @SneakyThrows
    @Override
    public Line next() {
        if (next==null) {
            fetchNext();
        }
        if (next==null) throw new NoSuchElementException();
        var result=next;
        next=null;
        return result;
    }

    private void fetchNext() throws IOException {
        next=readLine();
    }

    public class Line {

        private final String[] values;
        public String get(@NotNull String columnName) {
            return get(columnName,null);
        }
        public String get(int index) {
            return values[index];
        }

        public String get(int index,String defaultVal) {
            var val= values[index];
            return val.equals("")?defaultVal:val;
        }

        public String get(@NotNull String columnName,String defaultVal) {
            if (!columnNames.containsKey(columnName)) {
                return defaultVal;
            }
            var val=values[columnNames.get(columnName)];
            return val.equals("")?defaultVal:val;
        }

        private Line(@NotNull String[] values) {
            this.values=values;
        }

    }

    public int columnIndex(String columnName) {
        return columnNames.get(columnName);
    }

    public CsvReader(@NotNull InputStream stream,@NotNull char separator) throws IOException {
        internalReader=new BufferedReader(new InputStreamReader(stream));
        this.separator=separator;
        int i=0;
        String line=internalReader.readLine();
        if (line.charAt(0)=='\ufeff') { //TODO check if could be removed automatically
            line=line.substring(1);
        }
        for (String columnName : splitLine(line)) {
            columnNames.put(columnName.trim(),i++);
        }
    }

    public CsvReader(@NotNull InputStream stream) throws IOException {
        this(stream,',');
    }

    static public CsvReader reader(@NotNull InputStream stream) throws IOException {
        return new CsvReader(stream);
    }

    private final Map<String,Integer> columnNames=new HashMap<>();
    private final BufferedReader internalReader;
    private final char separator;

    public Line readLine() throws IOException {
        var line=internalReader.readLine();

        return line==null?null:new Line(splitLine(line));
    }

    private String[] splitLine(String line) {
        int tokenIndex=0;
        int first=0;
        ArrayList<String> resultList=new ArrayList<>(20);
        for (int i=0;i<line.length();++i) {
            if (line.charAt(i)==separator) {
                resultList.add(line.substring(first,i));
                first=i+1;
            } else if (line.charAt(i)=='"') {
                first=i+1;
                StringBuilder builder=new StringBuilder();
                while (true) {
                    while (++i<line.length() && line.charAt(i) != '"') ;
                    builder.append(line, first, i);
                    if (++i<line.length() && line.charAt(i) == '"') {
                        builder.append('"');
                        first = i + 1;
                    } else {
                        while (i<line.length() && line.charAt(i)!=separator) i++;
                        first=i+1;
                        resultList.add(builder.toString());
                        break;
                    }
                }
            }
        }
        resultList.add(line.substring(first));
        return resultList.toArray(new String[0]);
    }

    public void close() throws IOException {
        internalReader.close();
    }

}
