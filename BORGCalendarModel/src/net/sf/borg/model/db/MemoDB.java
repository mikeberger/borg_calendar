package net.sf.borg.model.db;

import java.util.Collection;

import net.sf.borg.model.Memo;

public interface MemoDB {

    public void addMemo(Memo m) throws DBException, Exception;

    public void delete(String name) throws DBException, Exception;

    public Collection getNames() throws Exception;

    public Collection readAll() throws DBException, Exception;

    public Memo readMemo(String name) throws DBException, Exception;

    public void updateMemo(Memo m) throws DBException, Exception;
    
    public void close() throws Exception;
    
    public Memo getMemoByPalmId(int id) throws Exception;

}