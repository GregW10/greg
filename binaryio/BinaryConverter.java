//
// Created by mario on 20/07/2022
//

package greg.binaryio;

import java.util.Scanner;
import java.util.InputMismatchException;

public class BinaryConverter {
    private static void one(Scanner sc) {
        System.out.print("Please enter whole number: ");
        long num;
        while (true) {
            try {
                num = sc.nextLong();
            } catch (InputMismatchException e) {
                System.out.print("You must only enter a numerical value: ");
                sc.nextLine();
                continue;
            }
            break;
        }
        BinaryNumber binNum = new BinaryNumber(num);
        System.out.println("In binary the number is: " + binNum);
    }
    private static void two(Scanner sc) throws BinaryFormatException {
        System.out.print("Please enter a binary number: ");
        sc.nextLine();
        String num;
        while (true) {
            try {
                num = sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.print("You must only enter a numerical value: ");
                sc.nextLine();
                continue;
            }
            break;
        }
        BinaryNumber binNum = new BinaryNumber(num);
        System.out.println("The decimal number is: " + binNum.getNumber());
    }
    public static void main(String [] args) throws BinaryFormatException {
        System.out.print("Input 1 for Decimal to Binary or 2 for Binary to Decimal: ");
        Scanner sc = new Scanner(System.in);
        int option;
        while (true) {
            try {
                option = sc.nextInt();
                if (option != 1 && option != 2) {
                    throw new InputMismatchException();
                }
            } catch (InputMismatchException e) {
                System.out.print("You must only enter 1 or 2: ");
                sc.nextLine();
                continue;
            }
            break;
        }
        switch (option) {
            case 1 -> one(sc);
            case 2 -> two(sc);
        }
    }
}
