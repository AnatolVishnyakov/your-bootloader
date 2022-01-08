package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TempFileGenerator {
    private static final String FILE_EXTENSION = ".part";

    private final YDProperties ydProperties;

    public File create(String fileName) {
        log.info("Create temp file {} to directory {}", fileName, ydProperties.getDownloadPath());
        File newTempFile = Paths.get(ydProperties.getDownloadPath())
                .resolve(prepareFileName(fileName))
                .toFile();

        if (newTempFile.exists()) {
            log.warn("File {} already exists!", fileName);
            return newTempFile;
        }

        try {
            if (!newTempFile.createNewFile()) {
                throw new RuntimeException("File can't create!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("File {} created success!", newTempFile.getName());
        return newTempFile;
    }

    @SneakyThrows
    public File create(String section, String fileName) {
        log.info("getOrCreate temp file {} to directory {}", fileName, ydProperties.getDownloadPath());

        Path folder = Paths.get(ydProperties.getDownloadPath())
                .resolve(section);
        if (!folder.toFile().exists()) {
            folder.toFile().mkdirs();
        }

        File newTempFile = folder
                .resolve(fileName)
                .toFile();

        if (newTempFile.exists()) {
            log.warn("File {} already exists!", fileName);
            return newTempFile;
        }

        newTempFile.createNewFile();
        return newTempFile;
    }

    public File get(String section, String fileName) {
        File file = Paths.get(ydProperties.getDownloadPath())
                .resolve(section)
                .resolve(fileName)
                .toFile();

        if (file.exists()) {
            log.warn("File {} already exists!", fileName);
            return file;
        }

        return null;
    }

    private String prepareFileName(String fileName) {
        fileName = transliterate(fileName);
        return new String(fileName.trim().getBytes(StandardCharsets.UTF_8))
                .concat(FILE_EXTENSION);
    }

    // TODO Убрать транслитерацию, реализовать
    //  работу с реальным именем вне зависимости
    //  от символов в имени
    private static String transliterate(String text) {
        char[] abcCyr = {'-', '.', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', ' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = {"-", ".", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", " ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (text.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        return builder.toString();
    }
}
