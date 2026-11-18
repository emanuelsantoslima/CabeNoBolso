package repository;

import model.Planejamento;
import java.io.*;

public class PersistenciaArquivoLocal implements IPersistenciaPlanejamento {

    @Override
    public void salvar(Planejamento planejamento, String caminhoArquivo) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(planejamento);
        }
    }

    @Override
    public Planejamento carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        File file = new File(caminhoArquivo);
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + caminhoArquivo);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Planejamento) ois.readObject();
        }
    }
}