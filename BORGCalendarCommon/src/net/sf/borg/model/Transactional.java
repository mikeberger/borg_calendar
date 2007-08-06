package net.sf.borg.model;

public interface Transactional {
    
    public void beginTransaction() throws Exception;
    public void commitTransaction() throws Exception;
    public void rollbackTransaction() throws Exception;
    

}
