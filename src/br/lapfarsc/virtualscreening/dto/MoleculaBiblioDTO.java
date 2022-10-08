package br.lapfarsc.virtualscreening.dto;

import java.util.List;

public class MoleculaBiblioDTO {

	private String idGeral;

	private String idMolecula;
	private String idSequencia;
	private String idSequenciaLigacaoAtomo;
	private String idSequenciaLigacaoNumerica;
	private String idLigacao;
	private List<String> listLigacao;
	
	private String metadados;
	private String head;
	private String body;

	public String getIdMolecula() {
		return idMolecula;
	}
	public void setIdMolecula(String idMolecula) {
		this.idMolecula = idMolecula;
	}
	public String getMetadados() {
		return metadados;
	}
	public void setMetadados(String metadados) {
		this.metadados = metadados;
	}
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getIdGeral() {
		return idGeral;
	}
	public void setIdGeral(String idGeral) {
		this.idGeral = idGeral;
	}
	public String getIdSequencia() {
		return idSequencia;
	}
	public void setIdSequencia(String idSequencia) {
		this.idSequencia = idSequencia;
	}

	public String getIdLigacao() {
		return idLigacao;
	}
	public void setIdLigacao(String idLigacao) {
		this.idLigacao = idLigacao;
	}
	public String getIdSequenciaLigacaoAtomo() {
		return idSequenciaLigacaoAtomo;
	}
	public void setIdSequenciaLigacaoAtomo(String idSequenciaLigacaoAtomo) {
		this.idSequenciaLigacaoAtomo = idSequenciaLigacaoAtomo;
	}
	public String getIdSequenciaLigacaoNumerica() {
		return idSequenciaLigacaoNumerica;
	}
	public void setIdSequenciaLigacaoNumerica(String idSequenciaLigacaoNumerica) {
		this.idSequenciaLigacaoNumerica = idSequenciaLigacaoNumerica;
	}
	public List<String> getListLigacao() {
		return listLigacao;
	}
	public void setListLigacao(List<String> listLigacao) {
		this.listLigacao = listLigacao;
	}
	
	
}

