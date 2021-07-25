package br.eti.arthurgregorio.sistemarquivos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.util.Locale;

public enum Command {

    LIST() {
        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("LIST") || commands[0].startsWith("list");
        }

        @Override
        Path execute(Path path) throws IOException {
//            System.out.println("Contidos na pasta: " + path);

            // acrescenta um separador ao final dos diretórios (para diferenciá-los de arquivos)
            Files.list(path)
                    .map(p -> p.getFileName() + (Files.isDirectory(p) ? File.separator : ""))
                    .forEach(System.out::println);

            return path;
        }
    },
    SHOW() {
        private String[] parameters = new String[]{};

        @Override
        void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("SHOW") || commands[0].startsWith("show");
        }

        @Override
        Path execute(Path path) {
            if (parameters.length == 1)
                throw new UnsupportedOperationException("Use: SHOW <nome do arquivo>");

            Path fileToRead = null;
            try {
                fileToRead = path.resolve(parameters[1]).toRealPath();
            } catch (FileNotFoundException | NoSuchFileException e) {
                throw new UnsupportedOperationException("Erro: arquivo não encontrado");
            } catch (IOException e) {
//                throw new UnsupportedOperationException("Erro desconhecido");
                e.printStackTrace();
            }

            if (Files.isDirectory(fileToRead))
                throw new UnsupportedOperationException("Erro: o caminho solicitado é um diretório.");

            String fileName = fileToRead.getFileName().toString().toLowerCase();
            if (!fileName.endsWith(".txt"))
                throw new UnsupportedOperationException(String.format("Erro: não há suporte ao arquivo (%s)", fileName));

            new FileReader().read(path);

            return path;
        }
    },
    BACK() {
        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("BACK") || commands[0].startsWith("back");
        }

        @Override
        Path execute(Path path) {
            var newPath = path.getParent();

            System.out.println("Estou agora em " + newPath);

            return path.getParent();
        }
    },
    OPEN() {
        private String[] parameters = new String[]{};

        @Override
        void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("OPEN") || commands[0].startsWith("open");
        }

        @Override
        Path execute(Path path) {

            if (parameters.length == 1)
                throw new UnsupportedOperationException("Use: OPEN <nome do arquivo>");

            Path newPath;
            try {
                newPath = path.resolve(parameters[1]).toRealPath();
            } catch (IOException e) {
                throw new UnsupportedOperationException("Erro: o diretório \""
                        + parameters[1] + "\" não existe");
            }

            if (Files.isRegularFile(newPath))
                throw new UnsupportedOperationException("Erro: é um arquivo, não um diretório.");

            System.out.println("Estou agora em: " + newPath);

            return newPath;
        }
    },
    DETAIL() {
        private String[] parameters = new String[]{};

        @Override
        void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("DETAIL") || commands[0].startsWith("detail");
        }

        @Override
        Path execute(Path path) {

            if (parameters.length == 1)
                throw new UnsupportedOperationException("Use: DETAIL <arquivo ou diretório>");

            Path newPath = null;
            try {
                newPath = path.resolve(parameters[1]).toRealPath();
            } catch (FileNotFoundException | NoSuchFileException e) {
                throw new UnsupportedOperationException("Erro: arquivo não encontrado");
            } catch (IOException e) {
//                throw new UnsupportedOperationException("Erro desconhecido");
                e.printStackTrace();
            }


            {
                BasicFileAttributeView view = Files.getFileAttributeView(newPath,
                        BasicFileAttributeView.class);
                BasicFileAttributes attributes = null;
                try {
                    attributes = view.readAttributes();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Is a directory? " + attributes.isDirectory());
                System.out.println("Is a regular file? " + attributes.isRegularFile());
                System.out.println("Is a symbolic link? " + attributes.isSymbolicLink());
                System.out.println("Is other? " + attributes.isOther());
                System.out.println("Size (in bytes): " + attributes.size());
                System.out.println("Last modified: " + attributes.lastModifiedTime());
                System.out.println("Last access: " + attributes.lastAccessTime());
            }


            if (false) {// DOS attributes
                DosFileAttributeView view = Files.getFileAttributeView(newPath,
                        DosFileAttributeView.class);
                DosFileAttributes attributes = null;
                try {
                    attributes = view.readAttributes();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Is archive? " + attributes.isArchive());
                System.out.println("Is hidden? " + attributes.isHidden());
                System.out.println("Is read-only? " + attributes.isReadOnly());
                System.out.println("Is system file? " + attributes.isSystem());
            }

            return path;
        }
    },
    EXIT() {
        @Override
        boolean accept(String command) {
            final var commands = command.split(" ");
            return commands.length > 0 && commands[0].startsWith("EXIT") || commands[0].startsWith("exit");
        }

        @Override
        Path execute(Path path) {
            System.out.print("Saindo...");
            return path;
        }

        @Override
        boolean shouldStop() {
            return true;
        }
    };

    abstract Path execute(Path path) throws IOException;

    abstract boolean accept(String command);

    void setParameters(String[] parameters) {
    }

    boolean shouldStop() {
        return false;
    }

    public static Command parseCommand(String commandToParse) {

        if (commandToParse.isBlank()) {
            throw new UnsupportedOperationException("Type something...");
        }

        final var possibleCommands = values();

        for (Command possibleCommand : possibleCommands) {
            if (possibleCommand.accept(commandToParse)) {
                possibleCommand.setParameters(commandToParse.split(" "));
                return possibleCommand;
            }
        }

        throw new UnsupportedOperationException("Can't parse command [%s]".formatted(commandToParse));
    }
}
