package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "terminal_group_param",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fk_terminal_group_id","name"})
})
public class TerminalGroupParam implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "terminalGroupParamSequence")
    @SequenceGenerator(name = "terminalGroupParamSequence", sequenceName = "seq_terminal_group_param", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_terminal_group_id", nullable = false, updatable = false)
    private TerminalGroup terminalGroup;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "value", nullable = false)
    private String value;

    public TerminalGroupParam(){}
    public TerminalGroupParam(TerminalGroup terminalGroup, String name, String value) {
        this.terminalGroup = terminalGroup;
        this.name = name;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TerminalGroup getTerminalGroup() {
        return terminalGroup;
    }

    public void setTerminalGroup(TerminalGroup terminalGroup) {
        this.terminalGroup = terminalGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TerminalGroupParam{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TerminalGroupParam)) return false;

        TerminalGroupParam that = (TerminalGroupParam) o;

        if (!name.equals(that.name)) return false;
        if (!terminalGroup.equals(that.terminalGroup)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = terminalGroup.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
