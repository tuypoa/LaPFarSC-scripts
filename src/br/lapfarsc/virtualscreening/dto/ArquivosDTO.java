package br.lapfarsc.virtualscreening.dto;

public class ArquivosDTO implements Comparable<ArquivosDTO>{

	private String filename;
	private String nome;
	private Integer indexLocal;
	private String indexLocalCompare;
	
	private Integer indexGlobal;
	
	
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Integer getIndexLocal() {
		return indexLocal;
	}
	public void setIndexLocal(Integer indexLocal) {
		this.indexLocal = indexLocal;
	}
	public Integer getIndexGlobal() {
		return indexGlobal;
	}
	public void setIndexGlobal(Integer indexGlobal) {
		this.indexGlobal = indexGlobal;
	}
	public String getIndexLocalCompare() {
		return indexLocalCompare;
	}
	public void setIndexLocalCompare(String indexLocalCompare) {
		this.indexLocalCompare = indexLocalCompare;
	}
	
	@Override
	public int compareTo(ArquivosDTO arg0) {
		if(this.indexLocalCompare==null) return 0;
		if(arg0.getIndexLocalCompare()==null) return 0;
		return this.indexLocalCompare.compareTo(arg0.getIndexLocalCompare());
	}
	
	
	
}
