package com.pr.nlp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CsvUtils {

    private static final String EMPTY = StringUtils.EMPTY;
    private static final char DOUBLE_QUOTE = '"';
    private static final String DOUBLE_QUOTE_STRING = DOUBLE_QUOTE + EMPTY;
    private static final char DEFAULT_DELIMITER = ';';
    private static final String DEFAULT_LINE_SEPARATOR = System.lineSeparator();

    public static <T> CsvUtilsWriter<T> writer(Class<T> clazz) {
        return new CsvUtilsWriter<T>();
    }

    public static <T> CsvUtilsReader<T> reader(Class<T> clazz) {
        return new CsvUtilsReader<T>();
    }

    static boolean isInQuote(String string) {
        return string != null && string.startsWith(DOUBLE_QUOTE_STRING) && string.endsWith(DOUBLE_QUOTE_STRING);
    }

    public static class CsvUtilsWriter<T> {
        CsvUtilsWriter() {  }

        private List<String> header;
        private List<String> footer;
        private List<T> content;
        private Function<T, List<String>> mapper;
        private Character delimiter = DEFAULT_DELIMITER;
        private String lineSeparator = DEFAULT_LINE_SEPARATOR;

        public CsvUtilsWriter<T> delimeter(char theDelimiter) {
            delimiter = theDelimiter;
            return this;
        }

        public CsvUtilsWriter<T> lineSeparator(String theLineSeparator) {
            lineSeparator = theLineSeparator;
            return this;
        }

        public CsvUtilsWriter<T> header(String... theHeader) {
            return header(Stream.of(theHeader).collect(Collectors.toList()));
        }
        public CsvUtilsWriter<T> header(List<String> theHeader) {
            header = theHeader;
            return this;
        }
        public CsvUtilsWriter<T> footer(String... theFooter) {
            return footer(Stream.of(theFooter).collect(Collectors.toList()));
        }
        public CsvUtilsWriter<T> footer(List<String> theFooter) {
            footer = theFooter;
            return this;
        }
        public CsvUtilsWriter<T> content(List<T> theContent) {
            content = theContent;
            return this;
        }
        public CsvUtilsWriter<T> mapper(Function<T, List<String>> theMapper) {
            mapper = theMapper;
            return this;
        }

        String csvLine(List<String> content) {
            return CollectionUtils.isEmpty(content) ? EMPTY :
                    content.stream()
                            .map(this::csvCell)
                            .collect(Collectors.joining(delimiter.toString()));
        }

        public String generate() {
            if (delimiter == null)
                throw new IllegalArgumentException("delimiter cannot be null");
            if (StringUtils.isEmpty(lineSeparator))
                throw new IllegalArgumentException("lineSeparator cannot be null");
            if (mapper == null)
                throw new IllegalArgumentException("mapper cannot be null");

            return Stream.of(csvLine(header), contentToCsvLines(), csvLine(footer))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining(lineSeparator));
        }

        private String contentToCsvLines() {
            return content == null ? EMPTY
                    : content.stream()
                    .map(mapper::apply)
                    .map(this::csvLine)
                    .collect(Collectors.joining(lineSeparator));
        }

        String csvCell(String string) {
            if (string == null)
                return EMPTY;

            string = string.replace(DOUBLE_QUOTE_STRING, DOUBLE_QUOTE_STRING + DOUBLE_QUOTE_STRING);

            //check if double_quote is needed
            if (string.contains(DOUBLE_QUOTE_STRING)
                    || string.contains(delimiter.toString())
                    || string.contains(lineSeparator)) {
                string = DOUBLE_QUOTE + string + DOUBLE_QUOTE;
            }
            return string;
        }

    }

    public static class CsvUtilsReader<T> {

        private String content;
        private boolean includeFirstLine = true;
        private boolean includeLastLine = true;
        private Function<CsvLine, T> mapper;
        private Character delimiter = DEFAULT_DELIMITER;
        private String lineSeparator = DEFAULT_LINE_SEPARATOR;
        private Predicate<String> csvLineFilter = e -> true;

        public CsvUtilsReader<T> delimeter(char theDelimiter) {
            delimiter = theDelimiter;
            return this;
        }
        public CsvUtilsReader<T> lineSeparator(String theLineSeparator) {
            lineSeparator = theLineSeparator;
            return this;
        }

        public CsvUtilsReader<T> includeFirstLine(boolean includeFirstLine) {
            this.includeFirstLine = includeFirstLine;
            return this;
        }
        public CsvUtilsReader<T> includeLastLine(boolean includeLastLine) {
            this.includeLastLine = includeLastLine;
            return this;
        }

        public CsvUtilsReader<T> content(String theContent) {
            this.content = theContent;
            return this;
        }
        public CsvUtilsReader<T> content(File file) throws FileNotFoundException, IOException {
            return content(file.toPath());
        }
        public CsvUtilsReader<T> content(Path path) throws IOException {
            return content(new String(Files.readAllBytes(path)));
        }
        public CsvUtilsReader<T> mapper(Function<CsvLine, T> theMapper) {
            mapper = theMapper;
            return this;
        }
        public CsvUtilsReader<T> csvLineFilter(Predicate<String> filter) {
            if (filter != null)
                csvLineFilter = filter;
            return this;
        }


        public List<T> read() {
            if (StringUtils.isEmpty(lineSeparator))
                throw new IllegalArgumentException("line separator cannot be empty, must provide a valid line separator");

            if (mapper == null)
                throw new IllegalArgumentException("line reader cannot be null");

            return splitLines()
                    .stream()
                    .map(this::splitCells)
                    .map(CsvLine::new)
                    .map(mapper::apply)
                    .collect(Collectors.toList());
        }

        private List<String> splitLines() {
            List<String> lines = Stream.of(content.split(lineSeparator)).collect(Collectors.toList());

            boolean flagReDo;

            do {
                flagReDo = false;
                for (ListIterator<String> i = lines.listIterator(); i.hasNext();) {
                    String line = i.next();
                    if (StringUtils.count(line, DOUBLE_QUOTE) % 2 == 1) {
                        i.remove();
                        String nextLine = i.next();
                        i.set(line + lineSeparator + nextLine);
                        flagReDo = true;
                        //break for loop, redo the checking from the beginning
                        break;
                    }
                }
            } while (flagReDo);


            if (!includeFirstLine) {
                lines.remove(0);
            }
            if (!includeLastLine) {
                lines.remove(lines.size() - 1);
            }

            return lines.stream()
                    .filter(csvLineFilter)
                    .collect(Collectors.toList());
        }

        private List<String> splitCells(String line) {
            List<String> cells = Stream.of(line.split(delimiter.toString())).collect(Collectors.toList());

            boolean flagReDo;
            do {
                flagReDo = false;
                for (ListIterator<String> i = cells.listIterator(); i.hasNext();) {
                    String cell = i.next();
                    if (StringUtils.count(cell, DOUBLE_QUOTE) % 2 == 1) {
                        i.remove();
                        String nextCell = i.next();
                        i.set(cell + delimiter + nextCell);
                        flagReDo = true;
                        break;
                    }
                }
            } while (flagReDo);

            return cells.stream()
                    .map(e -> {
                        if (isInQuote(e)) {
                            e = e.substring(1, e.length() - 1);
                        }
                        return e.replace(DOUBLE_QUOTE_STRING + DOUBLE_QUOTE_STRING, DOUBLE_QUOTE_STRING);
                    })
                    .collect(Collectors.toList());

        }

    }

    /**
     * Implements ArrayList<String> with safe {@link ArrayList#get(int)}<br>
     * does not throw {@link IndexOutOfBoundsException}
     *
     * @author iferdou
     *
     */
    public static class CsvLine extends ArrayList<String> {

        private static final long serialVersionUID = 1L;

        public CsvLine(List<String> cells) { super(cells); }

        /**
         * return null in case of {@link IndexOutOfBoundsException}
         */
        @Override
        public String get(int index) {
            try {
                return super.get(index);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

    }

}