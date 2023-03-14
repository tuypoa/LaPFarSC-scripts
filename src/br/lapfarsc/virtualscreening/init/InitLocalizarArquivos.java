package br.lapfarsc.virtualscreening.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.lapfarsc.virtualscreening.dto.MoleculaBiblioDTO;

public class InitLocalizarArquivos {

	
	public static String BIBLIO_PATH = "/home/LaPFarSC/biblio2700/";
	
	public static void main(String[] args) throws Exception {
		System.out.println("InitLocalizarArquivos: ");
		
		File path = new File(BIBLIO_PATH);
		if(!path.exists()) {
			System.out.println("--> PATH DOES NOT EXIST: "+path.getAbsolutePath());
			return;
		}		
		
		File pathArquivosParaInvestigar = new File(BIBLIO_PATH+"moleculas2774");
		File aqruivoBiblioteca = new File(BIBLIO_PATH+"moleculas2774.sdf");
		
		File[] arquivosParaInvestigar = pathArquivosParaInvestigar.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File arg0) {
				return arg0.getName().toLowerCase().endsWith(".sdf");
			}
		});
		
		int countEncontrou = 0;
		int countEncontrouMesmo = 0;
		int countEncontrouMols = 0;
		int countNaoEncontrou = 0;
		int countNaoEncontrouMols = 0;
		int countEncontrouLigacao = 0;
		int countEncontrouMaisUm = 0;
		int countDescartados = 0;
		int countGlobal = 0;
		try {
			HashMap<String, List<MoleculaBiblioDTO>> biblioBuscaGeral = new HashMap<String, List<MoleculaBiblioDTO>>();
			
			String bibliocont = loadTextFile(aqruivoBiblioteca);
			String[] moleculas = bibliocont.split("\\$\\$\\$\\$");
			System.out.print("> Arquivo Bilioteca de Moleculas: ");
			System.out.println(moleculas.length-1);
			for (String cont : moleculas) {
				if(cont.length()>5) {
					MoleculaBiblioDTO molDTO = parseStringMolecula(cont);
					
					
					if(biblioBuscaGeral.get(molDTO.getIdGeral())!=null){
						List<MoleculaBiblioDTO> l = biblioBuscaGeral.get(molDTO.getIdGeral());
						l.add(molDTO);
						biblioBuscaGeral.replace(molDTO.getIdGeral(), l);
					}else{
						List<MoleculaBiblioDTO> l = new ArrayList<MoleculaBiblioDTO>();
						l.add(molDTO);
						biblioBuscaGeral.put(molDTO.getIdGeral(), l);
					}
				}
			}
			
			int countTotal = 0;
			int countRepetidosTotal = 0;
			int countRepetidos = 0;
			//List<String> lidx = new ArrayList<String>();
			Set<String> idRem = biblioBuscaGeral.keySet();
			List<MoleculaBiblioDTO> lGlobal = new ArrayList<MoleculaBiblioDTO>();
			for (String idb : idRem) {
				List<MoleculaBiblioDTO> l = biblioBuscaGeral.get(idb);
				lGlobal.addAll(l);	
				if(l!=null && l.size()>1){
					countRepetidosTotal += l.size();
					countRepetidos++;
					//lidx.add(idb);
				//}else {
				}
				countTotal++;
			}
			//System.out.println(lGlobal.size());
			System.out.println("> BIBLIOTECA [");
			System.out.println("   "+ String.format("%04d", countRepetidos) + " IDS REPETIDOS ( "+ String.format("%04d", countRepetidosTotal) + " MOLS )");
			System.out.println("   "+ String.format("%04d", countTotal) + " IDS TOTAL");
			System.out.println("   = " + String.format("%04d", countTotal-countRepetidos) + " ( " + String.format("%04d",countTotal-countRepetidos+countRepetidosTotal) +" MOLS ) ]");
			
			//VARREDURA DE ARQUIVOS
			List<File> arquivosNaoEncontrados = new ArrayList<File>();
			List<MoleculaBiblioDTO> lRemove = new ArrayList<MoleculaBiblioDTO>();
			
			for (File file : arquivosParaInvestigar) {					
				String nome = file.getName();
				//System.out.println(nome);
				
				MoleculaBiblioDTO arquivoMolDTO = parseStringMolecula( loadTextFile(file) );
				if(arquivoMolDTO.getIdSequencia().indexOf(";")==-1) {
					countDescartados++;
					
					arquivoMolDTO.setHead( obterHeadPersonalizado(nome, "DESCARTADO") );
					gravarPastaNova("OUT-"+nome, arquivoMolDTO, "biblioteca-descartados/");
					
					//System.out.println(arquivoMolDTO.getBody());
				}else {
					if(biblioBuscaGeral.get(arquivoMolDTO.getIdGeral())!=null){
						List<MoleculaBiblioDTO> ldto = biblioBuscaGeral.get(arquivoMolDTO.getIdGeral());
						if(ldto.size()>1){
							countEncontrouMaisUm++;
							
							arquivoMolDTO.setHead( obterHeadPersonalizado(nome, "MAIS1") );
							gravarPastaNova("MAIS1-"+nome, arquivoMolDTO, "biblioteca-identificada/");
							
						}else {
							lRemove.addAll(ldto);
							countEncontrou++;
							
							MoleculaBiblioDTO dto = ldto.get(0);
							if(arquivoMolDTO.getIdSequenciaLigacaoNumerica().equals(dto.getIdSequenciaLigacaoNumerica()) ) {
								countEncontrouMesmo++;
							}
							
							arquivoMolDTO.setHead( obterHeadMetadados(nome, dto.getMetadados()) );
							arquivoMolDTO.setMetadados( dto.getMetadados() );
							gravarPastaNova("OK-"+nome, arquivoMolDTO, "biblioteca-identificada/");							
						}
					}else{					
						arquivosNaoEncontrados.add(file);
						countNaoEncontrou++;
						
						arquivoMolDTO.setHead( obterHeadPersonalizado(nome, "NOTFOUND") );
						gravarPastaNova("NOTFOUND-"+nome, arquivoMolDTO, "biblioteca-identificada/");

					}
				}
				countGlobal++;
			}
			
			countEncontrouMols = lRemove.size();
			lGlobal.removeAll(lRemove);
			countNaoEncontrouMols = lGlobal.size();
			
			//System.out.println(lGlobal.size());
			
			for (File file : arquivosNaoEncontrados) {
				//String nome = file.getName();
				MoleculaBiblioDTO arquivoMolDTO = parseStringMolecula( loadTextFile(file) );
				/*
				int ix = 0;
				for (MoleculaBiblioDTO dto : lGlobal) {
					if(dto.getIdSequencia().startsWith( arquivoMolDTO.getIdSequencia().substring(0,10) ) &&
							dto.getIdSequenciaLigacaoNumerica().startsWith( arquivoMolDTO.getIdSequenciaLigacaoNumerica().substring(0,30) )) {
						ix++;
						System.out.println(file.getName());
						System.out.println(arquivoMolDTO.getIdGeral());
						System.out.println(dto.getIdGeral());
						
						System.out.println(arquivoMolDTO.getIdSequenciaLigacaoNumerica());
						System.out.println(arquivoMolDTO.getIdSequenciaLigacaoAtomo());
						System.out.println();
						System.out.println(arquivoMolDTO.getIdSequenciaLigacaoNumerica().equals( dto.getIdSequenciaLigacaoNumerica() ) );
						System.out.println();
						System.out.println(dto.getIdSequenciaLigacaoNumerica());
						System.out.println(dto.getIdSequenciaLigacaoAtomo());
						System.out.println(dto.getMetadados());
						System.out.println("******");
					}
					
					if(ix==30) System.exit(0);
				}*/
				
				//nao encontrados
				for (MoleculaBiblioDTO dto : lGlobal) {
					if( dto.getIdLigacao().equals( arquivoMolDTO.getIdLigacao() ) && dto.getListLigacao().size() > 0) {
						int matchLigacao = 0;
						if(arquivoMolDTO.getListLigacao().size() == dto.getListLigacao().size()) {
							for (int i = 0; i<arquivoMolDTO.getListLigacao().size(); i++) {
								if(arquivoMolDTO.getListLigacao().get(i).equals(dto.getListLigacao().get(i))) {
									//System.out.println(arquivoMolDTO.getIdLigDupla().get(i)+ "=="+dto.getIdLigDupla().get(i));
									matchLigacao++; 
								}
							}
						}
						if(matchLigacao == arquivoMolDTO.getListLigacao().size()) {
							countEncontrouLigacao++;
							/*
							System.out.println(file.getName());
							System.out.println(arquivoMolDTO.getIdGeral());
							System.out.println(dto.getIdGeral());
							
							System.out.println(arquivoMolDTO.getIdSequenciaLigacaoNumerica());
							System.out.println(arquivoMolDTO.getIdSequenciaLigacaoAtomo());
							System.out.println();
							System.out.println(arquivoMolDTO.getIdSequenciaLigacaoNumerica().equals( dto.getIdSequenciaLigacaoNumerica() ) );
							System.out.println();
							System.out.println(dto.getIdSequenciaLigacaoNumerica());
							System.out.println(dto.getIdSequenciaLigacaoAtomo());
							System.out.println(dto.getMetadados());
							*/
							//System.out.println("******");
							//break;
						}
					}
				}
				//if(ok) break;
			}
			
			
		}finally {			
			System.out.println(">> ");
			System.out.println(">> ARQUIVOS [ ");
			System.out.println("   "+ String.format("%04d", countEncontrou) + " IDS IGUAIS ( "+ String.format("%04d", countEncontrouMols) +" MOLS ) ("+countEncontrouMesmo+") ");
			System.out.println("   "+ String.format("%04d", countNaoEncontrou) + " IDS NAO ENCONTRADOS ");
			System.out.println("   "+ String.format("%04d", countDescartados) + " ARQUIVOS DESCARTADOS");
			System.out.println("   ( BIBLIO COM "+ countNaoEncontrouMols +" MOLS ) ");
			System.out.println("       "+ String.format("%03d", countEncontrouMaisUm) + " ENCONTROU MAIS DE 1");
			System.out.println("       "+ String.format("%03d", countEncontrouLigacao) + " ENCONTRADOS POR LIGACAO ");
			
			
			System.out.println("   = " + String.format("%04d", countEncontrou+countNaoEncontrou+countDescartados) + " ( "+String.format("%04d", countGlobal )+" )  ]");
			
			System.out.println("> ");
			System.out.println("END.");	
		}
		
	}
	
	
	public static MoleculaBiblioDTO parseStringMolecula(String cont) {
	
		String[] linhas = cont.substring(0, cont.indexOf("M  END")!=-1?cont.indexOf("M  END"):cont.length() ).split("\n");

		//CATALOGO DE ATOMOS ========================
		StringBuilder biblioBuscaSequencia = new StringBuilder();
		StringBuilder sequenciaOriginal = new StringBuilder();
		List<String> listBonds = new ArrayList<String>();
		Integer ultimoAtomoSequencia = -1;
		for (int i = 0; i<linhas.length; i++) {
			if(linhas[i].length()>60){
				String atom = linhas[i].substring(31,33).trim();
				if(!atom.equals("H")){
					biblioBuscaSequencia.append(atom).append(";");
				}
				sequenciaOriginal.append(atom).append(";");
			}else if(linhas[i].length()>20 && linhas[i].length()<30){
				String atom1 = linhas[i].substring(0,3).trim();
				String atom2 = linhas[i].substring(3,6).trim();
				if(atom1.matches("[0-9]+") && atom2.matches("[0-9]+")) {
					listBonds.add(String.format("%03d", Integer.parseInt(atom1)) +";"+String.format("%03d", Integer.parseInt(atom2)));
					int atom = Integer.parseInt(atom1)>Integer.parseInt(atom2)? Integer.parseInt(atom1) : Integer.parseInt(atom2);
					if(atom > ultimoAtomoSequencia.intValue()) {
						ultimoAtomoSequencia = atom; 
					}
				}
			}
		}
		
		Collections.sort(listBonds);
		
		
		String tmpSeq = biblioBuscaSequencia.toString().substring(0, biblioBuscaSequencia.toString().length()-1);
		String tmpSeqOriginal = sequenciaOriginal.toString().substring(0, sequenciaOriginal.toString().length()-1);
		
		String sequenciaLigacaoNumerica = obterSequenciaNumericaPorLigacao(tmpSeqOriginal, listBonds);
		String sequenciaLigacaoAtomos = obterSequenciaPorLigacao(tmpSeqOriginal, sequenciaLigacaoNumerica);
		
		String[] seq1 = sequenciaLigacaoNumerica.split("-");
			
		//System.out.println(ultimoAtomoSequencia);
		int ultimoAtomoSequenciaLigacao = -1;
		Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(seq1[0]);
        while(m.find()) {
        	Integer numero = Integer.parseInt(m.group());
        	if(numero.intValue() > ultimoAtomoSequenciaLigacao) {
        		ultimoAtomoSequenciaLigacao = numero; 
			}
        }
		
		String[] as = tmpSeq.split(";");
		if(as.length > ultimoAtomoSequenciaLigacao) {
			biblioBuscaSequencia = new StringBuilder();
			for (int i = 0; i<ultimoAtomoSequenciaLigacao; i++) {
				biblioBuscaSequencia.append(as[i]);
				if(i < ultimoAtomoSequenciaLigacao-1) {
					biblioBuscaSequencia.append(";");
				}
			}
		}else {
			biblioBuscaSequencia = new StringBuilder( tmpSeq );
		}
		
		String[] atomos = biblioBuscaSequencia.toString().split(";");
		HashMap<String, Integer> countAtom = new HashMap<String, Integer>();
		for (int i = 0; i<atomos.length; i++) {
			String atom = atomos[i];
			Integer qtde = countAtom.get(atom);
			if(qtde==null) qtde = 0;
			countAtom.put(atom, qtde+1 );
		}
		Set<String> list = countAtom.keySet();
		List<String> lSort = new ArrayList<String>();
		for (String s : list) {
			//if(!s.toUpperCase().equals("Cl")){
				lSort.add( s +":"+countAtom.get(s) );
			//}
		}
		Collections.sort(lSort);
		StringBuilder sb = new StringBuilder();
		for (String s : lSort) {						
			sb.append(s).append(";");
		}	
		
		
		//CATALOGO DE LIGACOES ========================
		List<String> biblioBuscaLigacao = new ArrayList<String>();
		int countLigDupla = 0;
		int countLigTripla = 0;
		for (String string : linhas) {
			if(string.length()>20 && string.length()<30){
				String atom1 = string.substring(0,3).trim();
				String atom2 = string.substring(3,6).trim();
				if(atom1.matches("[0-9]+") && atom2.matches("[0-9]+")) {
					Integer a1 = Integer.parseInt(atom1);
					Integer a2 = Integer.parseInt(atom2);
					String lig = string.substring(7,9).trim();
					if(lig.matches("[0-9]+")) {
						Integer ligacao = Integer.parseInt( lig );
						String[] sequenciaOriginalArray = sequenciaOriginal.toString().split(";");
						if(!sequenciaOriginalArray[a1-1].equals("H") && !sequenciaOriginalArray[a2-1].equals("H")) {
							biblioBuscaLigacao.add( a1+";"+a2+";"+ligacao);
							if(ligacao.intValue() == 2) {								
								countLigDupla++;
							}else if(ligacao.intValue() == 3) {
								countLigTripla++;
							}
						}
					}
				}
			}
		}					
		
		String idMolecula = sb.toString();
		String idSequencia = biblioBuscaSequencia.toString();
		String idSequenciaRecursivaAtomo = sequenciaLigacaoAtomos;
		String idSequenciaRecursivaNumerica = sequenciaLigacaoNumerica;
		String idLigacao = "L"+biblioBuscaLigacao.size()+";D"+countLigDupla+";T"+countLigTripla;
		

		int indexInicial = cont.indexOf("\n", cont.indexOf("\n", cont.indexOf("\n")+1)+1 );					
		String meta = cont.substring( cont.indexOf("\n", cont.indexOf("END"))+1 ); //cont.substring( );
		if(meta.indexOf("$$$$")!=-1) {
			meta = meta.substring(0, meta.indexOf("$$$$"));
		}
		String head = cont.substring( 0, indexInicial );
		String body = cont.substring( indexInicial , cont.indexOf("\n", cont.indexOf("END"))+1 );
				
		String idGeral = idMolecula+"|"+idLigacao+"|"+idSequencia;
		
		/*
		//if(meta.contains("572924-54-0")) {
		//if(ultimoAtomoSequencia!=ultimoAtomoSequenciaLigacao) {
			System.out.println(idGeral);
			//System.out.println(meta);
			System.out.println(idSequencia);
			System.out.println(ultimoAtomoSequenciaLigacao);
			System.out.println(sequenciaLigacaoNumerica);
			System.out.println(biblioBuscaSequencia.toString());
			System.exit(0);
		//}
		*/
		
		MoleculaBiblioDTO dto = new MoleculaBiblioDTO();
		dto.setHead(head);
		dto.setMetadados(meta);
		dto.setBody(body);
		
		dto.setIdMolecula(idMolecula);
		dto.setIdGeral(idGeral);
		dto.setIdSequencia(idSequencia);
		dto.setIdSequenciaLigacaoAtomo(idSequenciaRecursivaAtomo);
		dto.setIdSequenciaLigacaoNumerica(idSequenciaRecursivaNumerica);
		dto.setIdLigacao(idLigacao);
		dto.setListLigacao(biblioBuscaLigacao);

		return dto;
	
	}
	
	public static String obterSequenciaPorLigacao(String seqAtomos, String seqNumericaPorLigacao) {
		String[] aryAtom = seqAtomos.split(";");
		Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(seqNumericaPorLigacao);
        while(m.find()) {
        	Integer numero = Integer.parseInt(m.group());
        	seqNumericaPorLigacao = seqNumericaPorLigacao.replaceFirst(m.group(), aryAtom[numero-1]);
        }
		return seqNumericaPorLigacao;
	}
	
	public static String obterSequenciaNumericaPorLigacao(String sequencia, List<String> listBonds) {
		String[] sequenciaArray = sequencia.split(";");
		StringBuilder sb = new StringBuilder();
		if(listBonds.size()>0) {
			List<String> lidos = new ArrayList<String>();
			int index = 0;
			while (true) {
				sb.append("[");
				for (int i=index ; i<listBonds.size(); i++) {
					index = i;
					String[] lig = listBonds.get(i).split(";");
					
					List<String> pais = new ArrayList<String>();
					//sb.append(lig[0]); 
					//pais.add(lig[0]);
					String r = obterSequenciaNumericaPorLigacaoRecursiva(sequenciaArray, lig[0], listBonds, pais, lidos);
					if(r.length()>0 && (sb.length()==0 || !lig[0].equals(r)) ) {
						sb.append("{").append(r).append("}");
					}
					
					if(listBonds.size() == lidos.size()) {
						break;
					}
					//verificar atual nos lidos, se tem link, se nao tiver pula
					boolean temLink = false;
					for (String lido : lidos) {
						String[] ligLido = lido.split(";");
						if(lig[0].equals(ligLido[0]) || lig[0].equals(ligLido[1]) || lig[1].equals(ligLido[0]) || lig[1].equals(ligLido[1])) {
							temLink = true;
							break;
						}
					}
					if(lidos.size()>0 && !temLink) {
						//index++;
						break;
					}
					//System.out.println("===="+lig[0]+"-"+lig[1]);
				}
				sb.append("]");
				
				/*
				List<String> listBondsTemp = new ArrayList<String>();
				for (String s : removerBonds) {
					if(!listBonds.contains(s)) {
						listBondsTemp.add(s);
					}
				}
				listBonds = listBondsTemp;
				*/
				//System.exit(0);
				//System.out.println(index+"*" + listBonds.size() +"-"+ lidos.size());
				if(listBonds.size() == lidos.size()) {
					break;
				}
				sb.append("-;-");
				//break;
			}
		}
		return sb.toString();
	}
	
	public static String obterSequenciaNumericaPorLigacaoRecursiva(String[] sequenciaArray, String atomo, List<String> listBonds, List<String> pais, List<String> lidos) {
		StringBuilder sb = new StringBuilder();
		for (String bond : listBonds) {
			boolean lido = false;
			for (String string : lidos) {
				if(bond.equals(string)) {
					lido = true;
					break;
				}
			}
			if(!lido) {
				String[] lig = bond.split(";");
				if(atomo.equals(lig[0])) {
					pais.add(atomo);
					if(!pais.contains(lig[1])) {
						String r = obterSequenciaNumericaPorLigacaoRecursiva(sequenciaArray, lig[1], listBonds, pais, lidos);
						if(r.length()>0) {
							if(!sequenciaArray[Integer.parseInt(lig[1])-1].equals("H")) {
								sb.append("{").append(r).append("}");
							}
							if(!lidos.contains(bond)) {
								lidos.add(bond);
							}
						}
					}
				}
			}
		}
		if(sb.length()>0) {
			return atomo + sb.toString();
		}else{
			return atomo;
		}
	}
	
	
	public static void gravarPastaNova(String filename, MoleculaBiblioDTO dto, String pastanova) throws IOException {
		File pasta = new File(BIBLIO_PATH + pastanova);
		if(!pasta.exists()){
			pasta.mkdir();
		}
		String conteudoOK = dto.getHead() + dto.getBody() + dto.getMetadados() + "\r\n$$$$";
		saveTextFile(new File(pasta.getPath()+"/" + filename), conteudoOK);
	}
	
	public static String obterHeadPersonalizado(String filename, String tag) {
		String head = filename.substring(0, filename.indexOf("."));							
		head += "\r\n"; //+ (heada[1].trim().length()==0? heada[0].trim() : heada[1].trim());
		head += "\r\n"+tag+"; "+ filename +"; LaPFarSC";
		return head;
	}
	
	public static String obterHeadMetadados(String filename, String metadados) {
		//String[] heada = headDto.split("\n");
		String head = "";							
		String molNome = metadados.substring( metadados.indexOf("\n", metadados.indexOf(">  <Name>"))+1, metadados.indexOf("\n", metadados.indexOf("\n", metadados.indexOf(">  <Name>"))+1 ) ).replace("\r", "").replace("\n", "");
		String molCat = metadados.substring( metadados.indexOf("\n", metadados.indexOf(">  <Cat>"))+1, metadados.indexOf("\n", metadados.indexOf("\n", metadados.indexOf(">  <Cat>"))+1 ) ).replace("\r", "").replace("\n", "");
		head = molCat + " " + molNome;
		head += "\r\n"; //+ (heada[1].trim().length()==0? heada[0].trim() : heada[1].trim());
		head += "\r\nOK; "+ filename +"; LaPFarSC";
		return head;
	}
	
	
	public static String loadTextFile(File file) throws Exception {
		FileReader fr = null;
	    BufferedReader br = null;
		try{
			StringBuilder conteudo = new StringBuilder();
			fr = new FileReader(file);											
			br = new BufferedReader(fr);
	        int read, N = 1024;
	        char[] buffer = new char[N];
	        
	        //int i = 0;			        
	        while(true) {
	            read = br.read(buffer, 0, N);
	            String text = new String(buffer, 0, read);
	            conteudo.append(text);
	            if(read < N){
	            	if(conteudo.length()>0){
	            		return conteudo.toString();
	            	}
	            	break;
	            }		            
	        }
		}catch(Exception e){
			System.out.println(file.getAbsolutePath());
			throw e;
		}finally{
			if(br!=null) br.close();
			if(fr!=null) fr.close();
		}
		return null;
	}

	public static void saveTextFile(File fileOutput, String conteudo) throws IOException {
		FileOutputStream fos = null;
		try{
			/*if(!fileOutput.exists()){
				fileOutput.mkdir();
			}*/
			fos = new FileOutputStream(fileOutput);
			fos.write(conteudo.getBytes());
			fos.flush();
		}finally{
			if(fos!=null) fos.close();
		}
	}
}
