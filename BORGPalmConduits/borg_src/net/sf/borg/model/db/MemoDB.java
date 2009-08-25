package net.sf.borg.model.db;

import java.util.Collection;

import net.sf.borg.model.entity.Memo;

public interface MemoDB {

    public void addMemo(Memo m) throws Exception;

    public void delete(String name) throws Exception;

    public Collection getNames() throws Exception;

    public Collection readAll() throws Exception;

    public Memo readMemo(String name) throws Exception;

    public void updateMemo(Memo m) throws Exception;
    
    public void close() throws Exception;
    
    public Memo getMemoByPalmId(int id) throws Exception;

}