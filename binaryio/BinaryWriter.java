//
// Created by mario on 20/07/2022
//

package greg.binaryio;

import java.util.Scanner;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryWriter {
    public static void main(String [] args) throws BinaryFormatException, IOException {
        Scanner sc = new Scanner(System.in);
        BinaryNumber binNum;
        System.out.print("Please enter file path to write: ");
        BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(sc.next()));
        } catch (IOException e) {
            System.out.println("An error occured: " + e);
            return;
        }
        System.out.println("Start writing (any invalid character causes termination): ");
        String number;
        while (sc.hasNext()) {
            number = sc.next();
            if (!number.matches("[\\s01]+")) {
                break;
            }
            out.write((int) (new BinaryNumber(number)).getNumber());
        }
        out.close();
    }
}