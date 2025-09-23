// Simulation.java
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Simulation {

    enum Estado { PRONTO, EXECUTANDO, BLOQUEADO, TERMINADO }

    static class Processo {
        final int pid;
        final int tempoTotalNecessario; 
        int TP;     
        int CP;  
        Estado estado;
        int NES;     
        int N_CPU; 

        Processo(int pid, int tempoTotalNecessario) {
            this.pid = pid;
            this.tempoTotalNecessario = tempoTotalNecessario;
            this.TP = 0;
            this.CP = this.TP + 1;
            this.estado = Estado.PRONTO;
            this.NES = 0;
            this.N_CPU = 0;
        }

        void atualizarCP() {
            this.CP = this.TP + 1;
        }

        boolean terminou() {
            return TP >= tempoTotalNecessario;
        }

        String dados() {
            return String.format("PID=%d, TP=%d/%d, CP=%d, EP=%s, NES=%d, N_CPU=%d",
                    pid, TP, tempoTotalNecessario, CP, estado, NES, N_CPU);
        }
    }

    private static final int NUM_PROCESSOS = 10;
    private static final int QUANTUM = 1000;
    private static final double IO_CHANCE_PER_CYCLE = 0.01; 
    private static final double UNBLOCK_CHANCE = 0.30; 

    private final Random random = new Random();
    private final Map<Integer, Processo> processos = new HashMap<>();
    private final Queue<Processo> filaProntos = new LinkedList<>();
    private final List<Processo> listaBloqueados = new ArrayList<>();
    private final File tabelaArquivo = new File("process_table.txt");

    public Simulation() {
        int[] tempos = {10000, 5000, 7000, 3000, 3000, 8000, 2000, 5000, 4000, 10000};
        for (int pid = 0; pid < NUM_PROCESSOS; pid++) {
            Processo p = new Processo(pid, tempos[pid]);
            processos.put(pid, p);
            filaProntos.add(p); 
        }
    }

    public void run() {
        int tempoGlobal = 0;
        while (!filaProntos.isEmpty() || !listaBloqueados.isEmpty()) {
            tentarDesbloquearProcessos();

            Processo atual = filaProntos.poll();
            if (atual == null) {
                continue;
            }

            atual.estado = Estado.EXECUTANDO;
            atual.N_CPU += 1;
            atual.atualizarCP();
            System.out.printf(">>> (PID %d) PRONTO >>> EXECUTANDO (restaurado). Dados: %s%n", atual.pid, atual.dados());

            int ciclosExecutadosNoQuantum = 0;
            boolean fezIO = false;
            while (ciclosExecutadosNoQuantum < QUANTUM && !atual.terminou()) {
                tempoGlobal++;
                ciclosExecutadosNoQuantum++;
                atual.TP += 1;
                atual.atualizarCP();

                if (random.nextDouble() < IO_CHANCE_PER_CYCLE) {
                    atual.NES += 1;
                    atual.estado = Estado.BLOQUEADO;
                    listaBloqueados.add(atual);
                    fezIO = true;
                    System.out.printf("(%d) EXECUTANDO >>> BLOQUEADO. Dados no momento da troca: %s%n", atual.pid, atual.dados());
                    gravarTabelaDeProcessos();
                    break; 
                }

                if (atual.terminou()) {
                    atual.estado = Estado.TERMINADO;
                    System.out.printf("### Processo %d TERMINOU. Parâmetros finais: %s%n", atual.pid, atual.dados());
                    gravarTabelaDeProcessos();
                    break;
                }
            }

            if (!fezIO && !atual.terminou()) {
                atual.estado = Estado.PRONTO;
                filaProntos.add(atual);
                System.out.printf("(%d) EXECUTANDO >>> PRONTO (quantum esgotado). Dados na troca: %s%n", atual.pid, atual.dados());
                gravarTabelaDeProcessos();
            }
        }

        System.out.println("=== Simulação finalizada: todos os processos terminaram. ===");
    }

    private void tentarDesbloquearProcessos() {
        if (listaBloqueados.isEmpty()) return;
        List<Processo> copia = new ArrayList<>(listaBloqueados);
        for (Processo p : copia) {
            if (random.nextDouble() < UNBLOCK_CHANCE) {
                // desbloqueia -> PRONTO
                p.estado = Estado.PRONTO;
                p.atualizarCP();
                listaBloqueados.remove(p);
                filaProntos.add(p);
                System.out.printf("(%d) BLOQUEADO >>> PRONTO (desbloqueado). Dados: %s%n", p.pid, p.dados());
            }
        }
    }

    private void gravarTabelaDeProcessos() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tabelaArquivo, false))) {
            writer.write(String.format("Tabela de Processos (atualizada em %s)%n", new Date()));
            writer.write("---------------------------------------------------------\n");
            List<Integer> pids = new ArrayList<>(processos.keySet());
            Collections.sort(pids);
            for (int pid : pids) {
                Processo p = processos.get(pid);
                writer.write(String.format("%s%n", p.dados()));
            }
        } catch (IOException e) {
            System.err.println("Erro ao gravar tabela de processos: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        Simulation sim = new Simulation();
        sim.run();
    }
}