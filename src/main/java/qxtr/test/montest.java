package qxtr.test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

//@Service
@Order(1)
public class montest implements CommandLineRunner  {

    @Override
    public void run(String... args) throws Exception {
        String line="IDFM:474351,,\"88, Rue Lepic / Moulin de la Galette\",,2.3368578394793236,48.88728005477054,,,1,,,,0,";
        char separator=',';
        /*String expr="([^\",]*)|\"([^\"]*)\"(,|$)";
        Scanner scanner=new Scanner(line);
        scanner.findAll(expr).forEach(result->System.out.println(result.group(1)));*/
        /*scanner.findInLine(expr);
        MatchResult result = scanner.match();
        for (int i=1; i<=result.groupCount(); i++)
            System.out.println(result.group(i));*/
        /*String token;
        while ((token=scanner.findInLine(expr))!=null) {
            System.out.println("token: "+token);
        };*/
        List<String> result=new LinkedList<>();
        int first=0;
        for (int i=0;i<line.length();++i) {
            if (line.charAt(i)==separator) {
                result.add(line.substring(first,i));
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
                        result.add(builder.toString());
                        break;
                    }
                }
            }
        }
        result.add(line.substring(first));
        result.forEach(s->System.out.println("Token: ["+s+']'));
        System.exit(1);
    }
}
