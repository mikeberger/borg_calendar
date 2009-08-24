package net.sf.borg.model;

/**
 * The Transaction interface is implemented by classes that provide database transactions
 * with commit and rollback
 */
public interface Transactional {
    
    /**
     * Begin transaction.
     * 
     * @throws Exception the exception
     */
    public void beginTransaction() throws Exception;
    
    /**
     * Commit transaction.
     * 
     * @throws Exception the exception
     */
    public void commitTransaction() throws Exception;
    
    /**
     * Rollback transaction.
     * 
     * @throws Exception the exception
     */
    public void rollbackTransaction() throws Exception;
    

}
