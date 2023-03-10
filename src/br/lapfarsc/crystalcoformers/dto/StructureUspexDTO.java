package br.lapfarsc.crystalcoformers.dto;

public class StructureUspexDTO {

	private Integer id;
	private String symLine;
	private Integer spaceGroup;
	private Boolean relaxDone;
	
	private String arquivoCif;
	
	private Integer countAtoms;
	
	private Integer countMol1;
	private Integer countMol2;
	private String ratio;
	
	private Double enthalpy;
	private Double volume;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getSymLine() {
		return symLine;
	}
	public void setSymLine(String symLine) {
		this.symLine = symLine;
	}
	public Integer getSpaceGroup() {
		return spaceGroup;
	}
	public void setSpaceGroup(Integer spaceGroup) {
		this.spaceGroup = spaceGroup;
	}
	public Boolean getRelaxDone() {
		return relaxDone;
	}
	public void setRelaxDone(Boolean relaxDone) {
		this.relaxDone = relaxDone;
	}
	public String getArquivoCif() {
		return arquivoCif;
	}
	public void setArquivoCif(String arquivoCif) {
		this.arquivoCif = arquivoCif;
	}
	public Integer getCountMol1() {
		return countMol1;
	}
	public void setCountMol1(Integer countMol1) {
		this.countMol1 = countMol1;
	}
	public Integer getCountMol2() {
		return countMol2;
	}
	public void setCountMol2(Integer countMol2) {
		this.countMol2 = countMol2;
	}
	public String getRatio() {
		return ratio;
	}
	public void setRatio(String ratio) {
		this.ratio = ratio;
	}
	public Integer getCountAtoms() {
		return countAtoms;
	}
	public void setCountAtoms(Integer countAtoms) {
		this.countAtoms = countAtoms;
	}
	public Double getEnthalpy() {
		return enthalpy;
	}
	public void setEnthalpy(Double enthalpy) {
		this.enthalpy = enthalpy;
	}
	public Double getVolume() {
		return volume;
	}
	public void setVolume(Double volume) {
		this.volume = volume;
	}
	

}
