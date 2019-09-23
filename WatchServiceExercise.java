package agibank.teste;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

public class WatchServiceExercise {
	
	private final static Path pathIn = Paths.get(System.getProperty("user.home").concat("/data/in"));
	private final static Path pathOut = Paths.get(System.getProperty("user.home").concat("/data/out"));
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
 
        pathIn.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
 
        WatchKey key;
        while ((key = watchService.take()) != null) {
        	for (WatchEvent<?> event : key.pollEvents()) {
                Thread.sleep(1000);//as vezes dava file not found
                lerArquivo(""+event.context());
            }
            key.reset();
        }
    }


	private static void lerArquivo(String fileName) {
		String line = "";
		try {
            //na primeira versão eu não tinha colocado o charset e estava tendo problemas no split com ç
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
            		new FileInputStream(pathIn+"\\"+fileName), Charset.forName("ISO-8859-1")));

			//variáveis necessárias para atender a regra de negócio do exercício proposto
            int quantidadeVendedores = 0;
            int quantidadeClientes = 0;
            Double valorMaiorVenda = (double) 0;
            String idMaiorVenda = "";
            Map<String, Double> vendedoresEValorVenda = new HashMap<>();
            
            
            while((line = bufferedReader.readLine()) != null) {
                String[] linha = line.split("\u00E7");
                String identificador = linha[0];
                
                switch(identificador) {
                	case "001" : 
                		quantidadeVendedores++;
                		break;
                	case "002" :
                		quantidadeClientes++;
                		break;
                	case "003" :
                		String saleId = linha[1];
                		Double valorVenda = descobrirValorVenda(linha);
                		if(valorMaiorVenda < valorVenda) {
                			valorMaiorVenda = valorVenda;
                			idMaiorVenda = saleId;
                		}
                		String salesManName = linha[3];                		
                		montarRelacaoVendedoresVendas(vendedoresEValorVenda, valorVenda, salesManName);
                		break;
                	default :
                		break;
                }
                if(idMaiorVenda.isEmpty()) {
                	idMaiorVenda = "Não há nenhuma venda no último arquivo processado.";
                }
            }   
            bufferedReader.close();         

            escreverArquivoSaida(fileName, quantidadeVendedores, quantidadeClientes, idMaiorVenda,
					vendedoresEValorVenda);
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'" + ex.getMessage());                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'" + ex.getMessage());                  
        }
		
	}


	private static void escreverArquivoSaida(String fileName, int quantidadeVendedores, int quantidadeClientes,
			String idMaiorVenda, Map<String, Double> vendedoresEValorVenda) throws IOException {
		
		FileWriter arq = new FileWriter(pathOut+"\\"+fileName);
		PrintWriter gravarArq = new PrintWriter(arq);
		
		gravarArq.printf("Quantidade de vendedores: " + quantidadeVendedores + "\n");
		gravarArq.printf("Quantidade de clientes: " + quantidadeClientes+ "\n");
		gravarArq.printf("Id da venda mais cara: " + idMaiorVenda+ "\n");
		
		String nomePiorVendedor = buscarNomePiorVendedor(vendedoresEValorVenda);
		
		gravarArq.printf("O pior vendedor: " + nomePiorVendedor + "\n");
		arq.close();
	}


	private static String buscarNomePiorVendedor(Map<String, Double> vendedoresEValorVenda) {
		Double menorValorVendas = Double.MAX_VALUE;
		String nomePiorVendedor = "";
		
		for (String key : vendedoresEValorVenda.keySet()) {
			if(vendedoresEValorVenda.get(key) < menorValorVendas) {
				nomePiorVendedor = key;
				menorValorVendas = vendedoresEValorVenda.get(key);
			}
		}
		return nomePiorVendedor;
	}


	private static Double descobrirValorVenda(String[] linha) {
		String[] item = linha[2].split("-");
		String itemQuantity = item[1];
		String itemPrice = item[2].replace("]", "").replace(",", ".");
		Double valorVenda = Double.parseDouble(itemPrice) * Integer.parseInt(itemQuantity);
		return valorVenda;
	}


	private static void montarRelacaoVendedoresVendas(Map<String, Double> vendedoresEValorVenda, Double valorVenda,
			String salesManName) {
		if(vendedoresEValorVenda.containsKey(salesManName)) {
			vendedoresEValorVenda.put(salesManName, vendedoresEValorVenda.get(salesManName) + valorVenda);
		} else {
			vendedoresEValorVenda.put(salesManName, valorVenda);
		}
	}
	
}
