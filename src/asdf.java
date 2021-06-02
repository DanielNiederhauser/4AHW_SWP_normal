import java.util.Scanner;

public class asdf {
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        String zeichenkette = "abc";
        String eingabe;
        double asdf;
        System.out.println("eingabe:");
        asdf=reader.nextDouble();
        System.out.println(asdf);
        double jklö=100.0;
        System.out.println(jklö);
        do{
            System.out.println("Zahl bitte: ");
            eingabe=reader.next();
        }while (istBuchstabenkette(eingabe));
        eingabe=null;
        do{
            System.out.println("Buchstabe bitte: ");
            eingabe=reader.next();
        }while (!istBuchstabenkette(eingabe));



    }
    static public boolean istBuchstabenkette(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }
}
