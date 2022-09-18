import mpi.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Класс, решающий следующую задачу:
 * релаизовать фильтрацию, используя неблокирующие обмены + Waitall() метод
 * (см. пример в файле task_3.png в корневой папке проекта)
 * @author Минин Кирилл
 * @version 0.0
 */

public class Main {
    public static void main(String[] args) throws IOException {
        // читаем массив с клавиатуры
        // не работает, почему-то не читает данные + сразу работает на всех процессорах только
        /*
        System.out.println("Hi! Input an array for sort (type 'esc' for end): ");
        ArrayList<Integer> list = new ArrayList<>();
        Scanner scan = new Scanner(System.in);
        String input;
        do {
            input = scan.nextLine();
            System.out.println("INP: "+input);
            list.add(Integer.valueOf(input));   // valueof преобразует строку в Integer. Может можно было parseInt (он в int переводит)
        } while (!input.equals("esc"));
        for (Integer elem : list) System.out.println(Integer.toString(elem));
        */

        // читаем массив из файла
        String inpPath = new File("").getAbsolutePath();
        inpPath += "\\src\\input.txt";
        BufferedReader reader = new BufferedReader(new FileReader(inpPath));
        String str;

        ArrayList<Integer> list = new ArrayList<>();
        while((str = reader.readLine()) != null ){
            if(!str.isEmpty()){
                list.add(Integer.valueOf(str));
            }}
        Integer[] intArr = list.toArray(new Integer[0]);

        String wsPath = new File("").getAbsolutePath();
        wsPath += "\\.idea\\workspace.xml";

        // лезем в xml конфигурации и изменяем кол-во процессов на n + 3
        File xmlFile = new File(wsPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // обновляем значения
            updateElementValue(doc, intArr.length + 3);

            // запишем отредактированный элемент в файл
            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(wsPath));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // считаем кол-во четных и нечетных эл-тов

        int cntEv = 0, cntNotEv = 0;

        for (Integer i : intArr)
            if (i % 2 == 0)
                cntEv++;
            else
                cntNotEv++;

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();

        // заполняем буфферы для каждого процесса (проверено, все норм)
        // заполнены будут 0..n-1 => n, n+1, n+2 - свободны (6,7,8 в тестовом случае)
        if (rank < intArr.length){
            int[] buf = new int[1];
            buf[0] = intArr[rank];
            //System.out.println();

            // если буффер хранит четное число, то отправляем на 6 проц
            // если НЕчетное, то на 7 проц
            if (buf[0] % 2 == 0) {
                MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, intArr.length, 2);
                //System.out.println("Proc " + rank + " buf = " + Arrays.toString(buf) + "\n    Yay! i'm even!");
            }
            else {
                MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, intArr.length + 1, 1);
                //System.out.println("Proc " + rank + " buf = " + Arrays.toString(buf) + "\n    Yay! i'm NOT even!");
            }
            System.out.println("--" + Arrays.toString(buf) + "         END OF "+rank+" --");
        }

        // принимаем отправленные с других процов четные числа
        if (rank == intArr.length){
            int[] buf = new int[cntEv];

            for (int i=0; i<cntEv; i++)
                MPI.COMM_WORLD.Recv(buf, i, 1, MPI.INT, MPI.ANY_SOURCE, 2);

            // сортируем массив и отправляем дальше его
            Arrays.sort(buf);
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, intArr.length + 2, 3);

            System.out.println("----" + Arrays.toString(buf) + "         END OF "+rank+" ----");
        }
        // принимаем отправленные с других процов НЕчетные числа
        if (rank == intArr.length + 1){
            int[] buf = new int[cntNotEv];

            for (int i=0; i<cntNotEv; i++)
                MPI.COMM_WORLD.Recv(buf,i,1,MPI.INT,MPI.ANY_SOURCE,1);

            // сортируем массив и отправляем дальше его
            Arrays.sort(buf);
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, intArr.length + 2, 3);

            System.out.println("----" + Arrays.toString(buf) + "         END OF "+rank+" ----");
        }

        if (rank == intArr.length + 2){
            int[] buf1 = new int[cntEv];
            int[] buf2 = new int[cntNotEv];
            int[] result = new int[cntEv+cntNotEv];

            MPI.COMM_WORLD.Recv(buf1,0,buf1.length,MPI.INT,intArr.length - 1 + 1,3);
            MPI.COMM_WORLD.Recv(buf2,0,buf2.length,MPI.INT,intArr.length + 1,3);

            System.out.println("----" + Arrays.toString(buf1) + " + " + Arrays.toString(buf2) + " = " + Arrays.toString(result) + "         END OF "+rank+" ----");

            result = mergeIntArrs(buf1, buf2, cntEv, cntNotEv);

            System.out.println("----" + Arrays.toString(buf1) + " + " + Arrays.toString(buf2) + " = " + Arrays.toString(result) + "         END OF "+rank+" ----");

        }

        // Вывод массива прочитанного 1 раз, проверено - все норм
        /*if (rank == 0)
        {
            System.out.println("\n----\nResponse from "+rank);
            for (Integer i : intArr) System.out.print(i + " ");
            System.out.println("\nWorkspace Path: "+wsPath+"\n----\n");
        }*/
        MPI.Finalize();
    }

    // изменяем значение существующего элемента name
    private static void updateElementValue(Document doc, int val) {
        NodeList opts = doc.getElementsByTagName("option");
        Element opt = null;
        // проходим по каждому элементу component
        for(int i=0; i<opts.getLength();i++){
            opt = (Element) opts.item(i); // приведение к типу Element
            if (opt.getAttribute("name").equals("VM_PARAMETERS")){
                opt.setAttribute("value","-jar H:\\mpj_v0.44\\lib\\starter.jar -np "+val);
            }
        }
    }

    public static int[] mergeIntArrs(int[] buf1, int[] buf2, int l1, int l2) {
        int[] merged = new int[l1 + l2];

        int p1, p2, mergedPos, cntr;
        p1 = p2 = mergedPos = cntr = 0;

        while(p1 < l1 && p2 < l2) {
            if (buf1[p1] < buf2[p2]) {
                merged[mergedPos++] = buf1[p1++];
            } else {
                merged[mergedPos++] = buf2[p2++];
            }
            cntr++;
            System.out.println("--------------- " + Arrays.toString(merged));
        }

        while (p1 < l1) {
            merged[mergedPos++] = buf1[p1++];
            cntr++;
            System.out.println("--------------- " + Arrays.toString(merged));
        }

        while (p2 < l2) {
            merged[mergedPos++] = buf2[p2++];
            cntr++;
            System.out.println("--------------- " + Arrays.toString(merged));
        }

        System.out.println("Steps count:" + cntr);

        return merged;
    }
}