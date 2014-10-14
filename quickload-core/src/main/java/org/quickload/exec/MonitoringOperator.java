package org.quickload.exec;

import org.quickload.record.Page;
import org.quickload.spi.PageOperator;
import org.quickload.spi.Report;

public class MonitoringOperator
        implements PageOperator
{
    private final PageOperator next;
    private Report report;
    private boolean failed;
    private boolean completed;

    public MonitoringOperator(PageOperator next)
    {
        this.next = next;
    }

    @Override
    public void addPage(Page page)
    {
        // TODO metrics collection
        next.addPage(page);
    }

    @Override
    public Report failed(Exception cause)
    {
        report = next.failed(cause);
        failed = true;
        return report;
    }

    @Override
    public Report completed()
    {
        report = next.completed();
        completed = true;
        return report;
    }

    @Override
    public void close() throws Exception
    {
        next.close();
    }

    public Report getReport()
    {
        return report;
    }

    public boolean isFailed()
    {
        return failed;
    }

    public boolean isCompleted()
    {
        return completed;
    }
}
