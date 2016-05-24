package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

/**
 * Created by lameroot on 24.05.16.
 */
public class Terminal {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "terminalSequence")
    @SequenceGenerator(name = "terminalSequence", sequenceName = "seq_terminal", allocationSize = 1)
    private Long id;
}
