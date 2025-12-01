import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


enum Estado {
    PRONTO,
    EXECUTANDO,
    BLOQUEADO,
    CONCLUIDO
}

class Processo {
    int pid;                
    int tempoTotalNecessario; 
    int tp;                 
    int cp;                
    Estado ep;             
    int nes;                
    int nCpu;             

    public Processo(int pid, int tempoTotalNecessario) {
        this.pid = pid;
        this.tempoTotalNecessario = tempoTotalNecessario;
        this.tp = 0;
        this.cp = 0;
        this.ep = Estado.PRONTO; 
        this.nes = 0;
        this.nCpu = 0;
    }

    @Override
    public String toString() {
        return String.format("PID: %d | TP: %d/%d | CP: %d | ESTADO: %s | NES: %d | N_CPU: %d",
                pid, tp, tempoTotalNecessario, cp, ep, nes, nCpu);
    }
}

public class SimuladorSO {

    private static final int QUANTUM = 1000;
    private static final String NOME_ARQUIVO = "tabela_processos.txt";
    private static Random random = new Random();

    public static void main(String[] args) {
      
        List<Processo> processos = new ArrayList<>();
        processos.add(new Processo(0, 10000));
        processos.add(new Processo(1, 5000));
        processos.add(new Processo(2, 7000));
        processos.add(new Processo(3, 3000));
        processos.add(new Processo(4, 3000));
        processos.add(new Processo(5, 8000));
        processos.add(new Processo(6, 2000));
        processos.add(new Processo(7, 5000));
        processos.add(new Processo(8, 4000));
        processos.add(new Processo(9, 10000));

        limparArquivo();

        System.out.println("--- INÍCIO DA SIMULAÇÃO ---");
        int processosConcluidos = 0;

        while (processosConcluidos < 10) {
            
            boolean todosBloqueadosOuConcluidos = true;

            for (Processo p : processos) {
                
               
                if (p.ep == Estado.CONCLUIDO) {
                    continue;
                }

                if (p.ep == Estado.BLOQUEADO) {
                    if (random.nextInt(100) < 30) {
                        System.out.println(">>> (PID " + p.pid + ") DESBLOQUEOU: BLOQUEADO -> PRONTO");
                        p.ep = Estado.PRONTO;
                    } else {
                        continue;
                    }
                }
                if (p.ep == Estado.PRONTO) {
                    todosBloqueadosOuConcluidos = false;
                    
                    p.ep = Estado.EXECUTANDO;
                    p.nCpu++;
                    ResultadoExecucao resultado = executarProcesso(p);

                    Estado estadoAnterior = Estado.EXECUTANDO;
                    Estado novoEstado = null;

                    switch (resultado) {
                        case IO_REQUEST:
                            novoEstado = Estado.BLOQUEADO;
                            p.nes++;
                            break;
                        case QUANTUM_EXPIRED:
                            novoEstado = Estado.PRONTO;
                            break;
                        case FINISHED:
                            novoEstado = Estado.CONCLUIDO;
                            processosConcluidos++;
                            break;
                    }

                    p.ep = novoEstado;

                    if (novoEstado == Estado.CONCLUIDO) {
                        System.out.println("!!! (PID " + p.pid + ") FINALIZADO !!! Parâmetros finais:");
                        System.out.println(p.toString());
                    } else {
                        System.out.println("(PID " + p.pid + ") " + estadoAnterior + " >>>> " + novoEstado);
                    }

                    salvarDadosProcesso(p);
                }
            }
            if (todosBloqueadosOuConcluidos && processosConcluidos < 10) {
            }
        }
        
        System.out.println("--- FIM DA SIMULAÇÃO ---");
    }

    enum ResultadoExecucao {
        QUANTUM_EXPIRED,
        IO_REQUEST,
        FINISHED
    }

    private static ResultadoExecucao executarProcesso(Processo p) {
        
        for (int ciclo = 0; ciclo < QUANTUM; ciclo++) {
            
            if (p.tp >= p.tempoTotalNecessario) {
                return ResultadoExecucao.FINISHED;
            }

            p.tp++;
            p.cp = p.tp + 1;

            if (p.tp < p.tempoTotalNecessario) {
                if (random.nextInt(100) < 1) {
                    return ResultadoExecucao.IO_REQUEST;
                }
            }
        }

        if (p.tp >= p.tempoTotalNecessario) {
            return ResultadoExecucao.FINISHED;
        }

        return ResultadoExecucao.QUANTUM_EXPIRED;
    }

    private static void salvarDadosProcesso(Processo p) {
        try (FileWriter fw = new FileWriter(NOME_ARQUIVO, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(p.toString());
        } catch (IOException e) {
            System.err.println("Erro ao gravar arquivo: " + e.getMessage());
        }
    }

    private static void limparArquivo() {
        try (FileWriter fw = new FileWriter(NOME_ARQUIVO, false)) {
      
        } catch (IOException e) {
          
        }
    }
}