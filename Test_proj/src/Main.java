import mpi.*;

public class Main {
    public static void main(String[] args) {
        // System.out.println("Hello world!");
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello from ."+rank+".");
        MPI.Finalize();
    }
}