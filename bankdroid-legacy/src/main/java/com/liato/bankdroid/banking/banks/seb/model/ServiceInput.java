package com.liato.bankdroid.banking.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServiceInput implements Serializable {

    @JsonProperty("Condition")
    private String condition;
    @JsonProperty("VariableNamePossibleValues")
    private List<Object> variableNamePossibleValues = new ArrayList<Object>();
    @JsonProperty("VariableName")
    private String variableName;
    @JsonProperty("VariableValue")
    private String variableValue;

    @JsonProperty("Condition")
    public String getCondition() {
        return condition;
    }

    @JsonProperty("Condition")
    public void setCondition(String condition) {
        this.condition = condition;
    }

    @JsonProperty("VariableNamePossibleValues")
    public List<Object> getVariableNamePossibleValues() {
        return variableNamePossibleValues;
    }

    @JsonProperty("VariableNamePossibleValues")
    public void setVariableNamePossibleValues(List<Object> variableNamePossibleValues) {
        this.variableNamePossibleValues = variableNamePossibleValues;
    }

    @JsonProperty("VariableName")
    public String getVariableName() {
        return variableName;
    }

    @JsonProperty("VariableName")
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @JsonProperty("VariableValue")
    public String getVariableValue() {
        return variableValue;
    }

    @JsonProperty("VariableValue")
    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }

}
