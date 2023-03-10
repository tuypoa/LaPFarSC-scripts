package br.lapfarsc.crystalcoformers.dto;

import java.util.HashMap;

public class MoleculeUspexDTO {

	private String nome;
	private String conteudo;
	private Integer qtdeAtoms;
	private HashMap<String, Integer> freqAtoms;
	
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getConteudo() {
		return conteudo;
	}
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	public Integer getQtdeAtoms() {
		return qtdeAtoms;
	}
	public void setQtdeAtoms(Integer qtdeAtoms) {
		this.qtdeAtoms = qtdeAtoms;
	}
	public HashMap<String, Integer> getFreqAtoms() {
		return freqAtoms;
	}
	public void setFreqAtoms(HashMap<String, Integer> freqAtoms) {
		this.freqAtoms = freqAtoms;
	}
	
	
	
}
