import mpi.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
        BufferedReader reader = new BufferedReader(new FileReader("G:\\Github Repositories\\distributed-systems\\Test_proj\\src\\input.txt"));
        String str;

        ArrayList<String> list = new ArrayList<>();
        while((str = reader.readLine()) != null ){
            if(!str.isEmpty()){
                list.add(str);
            }}
        String[] stringArr = list.toArray(new String[0]);

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        if (rank == 0)
        {
            System.out.println("\nResponse from "+rank);
            for (String s : stringArr) System.out.print(s + " ");
        }
        MPI.Finalize();
    }
}