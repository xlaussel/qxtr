package qxtr.utils;


import com.sun.istack.NotNull;
import lombok.SneakyThrows;

import java.io.*;
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
            return values[columnNames.get(columnName)];
        }
        public String get(int index) {
            return values[index];
        }

        public String get(int index,String defaultVal) {
            var val= values[index];
            return val.equals("")?defaultVal:val;
        }

        public String get(@NotNull String columnName,String defaultVal) {
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
        this.separator=String.valueOf(separator);
        int i=0;
        for (String columnName : internalReader.readLine().split(this.separator)) {
            columnNames.put(columnName,i++);
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
    private final String separator;

    public Line readLine() throws IOException {
        var line=internalReader.readLine();
        return line==null?null:new Line(line.split(separator));
    }

    public void close() throws IOException {
        internalReader.close();
    }

}
