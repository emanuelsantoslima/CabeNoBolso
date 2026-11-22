package repository;

import model.Planejamento;
import java.io.*;

public class PersistenciaArquivoLocal implements IPersistenciaPlanejamento {

    @Override
    public void salvar(Planejamento planejamento, String caminhoArquivo) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(caminhoArquivo);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(planejamento);
        }
    }

    @Override
    public Planejamento carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        File file = new File(caminhoArquivo);
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + caminhoArquivo);
        }
        if (file.length() == 0) {
            throw new IOException("O arquivo está vazio.");
        }
        String corrompido = "O arquivo está corrompido ou não foi gravado por esta versão do programa.";

        Object lido;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            lido = ois.readObject();
        } catch (EOFException e) {
            throw new IOException("O arquivo está incompleto. Ele pode ter sido cortado ao ser copiado ou gravado.");
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            // um arquivo estragado falha de vários jeitos na leitura, e para o usuário é tudo o mesmo problema
            throw new IOException(corrompido);
        }

        if (!(lido instanceof Planejamento)) {
            throw new IOException("O arquivo não contém um planejamento do Cabe no Bolso.");
        }
        Planejamento planejamento = (Planejamento) lido;
        if (!planejamento.estaConsistente()) {
            throw new IOException(corrompido);
        }
        return planejamento;
    }
}