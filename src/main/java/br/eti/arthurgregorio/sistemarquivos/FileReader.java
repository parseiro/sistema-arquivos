package br.eti.arthurgregorio.sistemarquivos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader {

    public void read(Path path) {
        try (var linhas = Files.lines(path)) {
            linhas
                    //.limit(1000) // opcionalmente impor um limite no numero de linhas
                    .forEach(System.out::println);
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException("Arquivo não encontrado");
        } catch (AccessDeniedException e) {
//            e.printStackTrace();
            throw new UnsupportedOperationException("Erro: acesso negado");
        } catch (IOException e) {
//            e.printStackTrace();
            throw new UnsupportedOperationException("Erro desconhecido");
        }
    }
}
