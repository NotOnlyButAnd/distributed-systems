import mpi.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

        ArrayList<String> list = new ArrayList<>();
        while((str = reader.readLine()) != null ){
            if(!str.isEmpty()){
                list.add(str);
            }}
        String[] stringArr = list.toArray(new String[0]);

        String wsPath = new File("").getAbsolutePath();
        wsPath += "\\.idea\\workspace.xml";

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        if (rank == 0)
        {
            System.out.println("\nResponse from "+rank);
            for (String s : stringArr) System.out.print(s + " ");
            System.out.println("\nWorkspace Path: "+wsPath);
            
            // лезем в xml конфигурации и изменяем кол-во процессов на n + 3
            File xmlFile = new File(wsPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;

            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlFile);
                doc.getDocumentElement().normalize();

                // обновляем значения
                updateElementValue(doc, stringArr.length + 3);

                // запишем отредактированный элемент в файл
                doc.getDocumentElement().normalize();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(wsPath));
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
                System.out.println("XML successfully changed!");

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        MPI.Finalize();
    }

    // изменяем значение существующего элемента name
    private static void updateElementValue(Document doc, int val) {
        NodeList opts = doc.getElementsByTagName("option");
        Element opt = null;
        // проходим по каждому элементу component
        for(int i=0; i<opts.getLength();i++){
            opt = (Element) opts.item(i); // приведение к типу Element
            //System.out.println("  curr name: "+opt.getAttribute("name"));
            if (opt.getAttribute("name").equals("VM_PARAMETERS")){
                //System.out.print("    YAY: "+opt.getAttribute("value"));
                opt.setAttribute("value","-jar H:\\mpj_v0.44\\lib\\starter.jar -np "+val);
                //System.out.println("    now...: "+opt.getAttribute("value"));
            }
        }
    }
}