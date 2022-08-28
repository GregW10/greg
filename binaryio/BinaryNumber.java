//
// Created by mario on 20/07/2022
//

package greg.binaryio;

import java.lang.StringBuilder;

public class BinaryNumber {
    private long num;
    private String binaryNumber;
    private void setBinaryString() {
        StringBuilder number = new StringBuilder();
        if (num == 0) {
            number.append("00000000");
            binaryNumber = number.toString();
            return;
        }
        long temp = num;
        int count = 0;
        int spaces = 0;
        while (temp != 0) {
            if (count % 8 == 0 && count >= 8) {
                number.append(' '); ++spaces;
            }
            number.append(1 & temp);
            temp >>= 1;
            ++count;
        }
        number.reverse();
        while (((number.length() - spaces) & (number.length() - spaces - 1)) != 0 || number.length() < 8) {
            if ((number.length() - spaces) % 8 == 0) {
                number.insert(0, ' '); ++spaces;
            }
            number.insert(0, '0');
        }
        binaryNumber = number.toString();
    }
    private void setBinaryNumber() {
        int i = binaryNumber.indexOf('1');
        if (i == -1) {
            num = 0;
            return;
        }
        long Number = 1;
        char c;
        ++i;
        for (; i < binaryNumber.length(); ++i) {
            c = binaryNumber.charAt(i);
            if (c == 32) {
                continue;
            }
            Number <<= 1;
            Number += c - 48;
        }
        num = Number;
    }
    BinaryNumber() {
        num = 0;
        setBinaryString();
    }
    BinaryNumber(long Number) {
        num = Number;
        setBinaryString();
    }
    BinaryNumber(String binaryNumberString) throws BinaryFormatException {
        char c;
        long num_count = 0;
        for (int i = 0; i < binaryNumberString.length(); ++i) {
            c = binaryNumberString.charAt(i);
            if (c == '1' || c == '0') ++num_count;
            if (c != '0' && c != '1' && c != ' ') {
                throw new BinaryFormatException();
            }
        }
        if (num_count == 0) {
            throw new BinaryFormatException();
        }
        binaryNumber = binaryNumberString;
        setBinaryNumber();
    }
    public void setBinaryString(String newBinaryStringNumber) {
        binaryNumber = newBinaryStringNumber;
        setBinaryNumber();
    }
    public void setNumber(long newNumber) {
        num = newNumber;
        setBinaryString();
    }
    public long getNumber() {
        return num;
    }
    @Override public String toString() {
        return binaryNumber;
    }
}