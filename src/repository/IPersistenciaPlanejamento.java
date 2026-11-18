package repository;

import model.Planejamento;
import java.io.IOException;

public interface IPersistenciaPlanejamento {
    void salvar(Planejamento planejamento, String caminhoArquivo) throws IOException;
    Planejamento carregar(String caminhoArquivo) throws IOException, ClassNotFoundException;
}