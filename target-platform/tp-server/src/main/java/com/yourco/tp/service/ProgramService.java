package com.yourco.tp.service;

import com.yourco.tp.model.ProgramSpec;
import org.springframework.stereotype.Service;
import com.yourco.tp.util.TpValidator;

import java.util.*;

@Service
public class ProgramService {
    private final Map<String, ProgramSpec> programs = new LinkedHashMap<>();
    private final StateStore stateStore;
    private final ProgramSupervisor supervisor;
    private final AuditLogService audit;
    private final TpValidator validator = new TpValidator();

    public ProgramService(StateStore stateStore, ProgramSupervisor supervisor, AuditLogService audit) {
        this.stateStore = stateStore;
        this.supervisor = supervisor;
        this.audit = audit;
    }

    public List<ProgramSpec> list() { return new ArrayList<>(programs.values()); }

    public void loadAll(List<ProgramSpec> specs) {
        programs.clear();
        for (ProgramSpec s : specs) programs.put(s.getId(), s);
    }

    public ProgramSpec install(ProgramSpec spec) {
        if (spec.getId() == null || spec.getId().isBlank()) throw new IllegalArgumentException("id required");
        validator.validate(spec);
        programs.putIfAbsent(spec.getId(), spec);
        spec = programs.get(spec.getId());
        if (spec.getStatus() == null || spec.getStatus().isBlank()) spec.setStatus("installed");
        stateStore.savePrograms(list());
        audit.program("INSTALL", spec.getId(), Map.of());
        return spec;
    }

    public ProgramSpec start(String id) {
        ProgramSpec spec = get(id);
        spec.setStatus("running");
        try { supervisor.start(spec); } catch (RuntimeException e) { spec.setStatus("installed"); throw e; }
        stateStore.savePrograms(list());
        audit.program("START", id, Map.of());
        return spec;
    }

    public ProgramSpec stop(String id) {
        ProgramSpec spec = get(id);
        spec.setStatus("stopped");
        supervisor.stop(id);
        stateStore.savePrograms(list());
        audit.program("STOP", id, Map.of());
        return spec;
    }

    private ProgramSpec get(String id) {
        ProgramSpec spec = programs.get(id);
        if (spec == null) throw new NoSuchElementException("program not found: " + id);
        return spec;
    }
}
