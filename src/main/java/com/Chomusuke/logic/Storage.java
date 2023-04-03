/*  Accountable: a personal spending monitoring program
    Copyright (C) 2023  Artur Yukhanov

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.chomusuke.logic;

import com.chomusuke.util.Preconditions;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.chomusuke.logic.Transaction.TransactionType;
import static com.chomusuke.logic.Transaction.ValueType;

/**
 * Provides disk storage for transactions.
 */
public class Storage {

    private static final int MAX_TRANSACTION_COUNT = 512;

    public static final Path ROOT_DIR = Path.of(System.getProperty("user.home")).resolve(System.getProperty("os.name").equals("Mac OS X") ? "Library/Application Support" : "AppData/Roaming");
    private static final Path DIR_NAME = ROOT_DIR.resolve("Accountable/storage/");

    /**
     * Don't let anyone instantiate this class.
     */
    private Storage() {}

    /**
     * Appends the specified {@code Transaction} to a file according to the given year and month.
     * This method does not overwrite existing data in the file.
     *
     * @param t  a {@code Transaction}
     * @param year  a value
     * @param month a value
     */
    public static void write(Transaction t, int year, int month) {
        Preconditions.checkArgument(month > 0 && month <= 12);

        Path file = DIR_NAME.resolve(String.format("%s/%s", year, month));

        createDirFiles(file);

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(file.toString(), true))) {

            output.writeUTF(t.name());
            output.writeByte(t.to());
            output.writeByte(t.packTypes());
            output.writeFloat(t.value());

            System.out.println("Wrote 1 transaction");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the specified list to a file according to the given year and month.
     * This method overwrites any existing date in the file.
     *
     * @param list  a {@code Transaction} list
     * @param year  a value
     * @param month a value
     */
    public static void write(List<Transaction> list, int year, int month) {
        Preconditions.checkArgument(month > 0 && month <= 12);

        Path file = DIR_NAME.resolve(String.format("%s/%s", year, month));

        createDirFiles(file);

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(file.toString()))) {

            for (Transaction t : list) {
                output.writeUTF(t.name());
                output.writeByte(t.to());
                output.writeByte(t.packTypes());
                output.writeFloat(t.value());
            }

            System.out.printf("Wrote %s transactions%n", list.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the file located at year/month.
     *
     * @param year a value
     * @param month a value
     *
     * @return a list of transactions loaded from the file
     */
    public static List<Transaction> load(int year, int month) {
        Preconditions.checkArgument(month > 0 && month <= 12);

        Path file = DIR_NAME.resolve(String.format("%s/%s", year, month));

        LinkedList<Transaction> txs = new LinkedList<>();

        try (DataInputStream input = new DataInputStream(new FileInputStream(file.toString()))) {
            for (int i = 0 ; i < MAX_TRANSACTION_COUNT ; i++) {
                String name = input.readUTF();

                byte to = input.readByte();

                byte types = input.readByte();
                TransactionType tt = TransactionType.of((byte) (types >>> 2));
                ValueType vt = ValueType.of(types);

                float value = input.readFloat();

                Transaction newTx = new Transaction(name, to, tt, vt, value);
                if (tt.equals(TransactionType.REVENUE))
                    txs.add(0, newTx);
                else
                    txs.add(newTx);
            }

            throw new RuntimeException(String.format("The file %s/%s contains too much transactions.", year, month));
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (EOFException ignored) {
            // Exception ignored
            System.out.println("Reached end of file.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return txs;
    }

    /**
     * Returns the files that exist in the corresponding
     * year's directory.
     * <br>
     * The files need to be named as integers
     * in order to be counted.
     *
     * @param year a value
     *
     * @return a list of available months
     */
    public static List<String> getAvailableMonths(int year) {

        return getAvailable(Integer.toString(year));
    }

    /**
     * Returns the directories that exist in the
     * global storage directory.
     * <br>
     * The directories need to be named as integers
     * in order to be counted.
     *
     * @return a list of available years
     */
    public static List<String> getAvailableYears() {

        return getAvailable("");
    }

    /**
     * Lists the existing files or directories at the given path.
     * <br>
     * The files/directories need to be named as integers
     * in order to be counted.
     *
     * @param path a path
     *
     * @return a list of files/directories
     */
    private static List<String> getAvailable(String path) {

        List<String> valid = new ArrayList<>();
        File[] availableF = new File(DIR_NAME.resolve(path).toString()).listFiles();

        if (availableF != null) {
            for (File f : availableF) {
                // Filters out non-integer-named files and directories
                try {
                    Integer.parseInt(f.getName());

                    valid.add(f.getName());
                } catch (NumberFormatException ignored) {
                    // Exception ignored
                }
            }
        }

        Collections.sort(valid);

        return valid;
    }

    /**
     * Creates the directory and file corresponding to the path
     * {@code DIR_NAME}/a/b
     *
     * @param p the path to the file
     */
    private static void createDirFiles(Path p) {

        try {
            Files.createDirectories(p.getParent());
            Files.createFile(p);
        } catch (FileAlreadyExistsException ignored) {
            // Exception ignored
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}