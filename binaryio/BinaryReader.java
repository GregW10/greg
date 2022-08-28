//
// Created by mario on 20/07/2022
//

package greg.binaryio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.lang.Thread;

public class BinaryReader {
    public static void main(String [] args) throws IOException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        BinaryNumber binNum = new BinaryNumber();
        System.out.print("Input file path: ");
        BufferedInputStream fReader;
        try {
            fReader = new BufferedInputStream(new FileInputStream(sc.next()));
        } catch (FileNotFoundException e) {
            System.out.println("File reading error: file not found.");
            return;
        }
        long numPrints = 0;
        int i;
        while ((i = fReader.read()) != -1) {
            if (numPrints++ % 10 == 0) {
                System.out.print("\n");
            }
            Thread.sleep(1000);
            binNum.setNumber(i);
            System.out.print(binNum + " ");
        }
        fReader.close();
        System.out.println("\n\nReached end of file.");
    }
}