package org.quickload.standards;

import javax.validation.constraints.NotNull;
import com.google.common.base.Function;
import com.google.inject.Inject;
import org.quickload.buffer.Buffer;
import org.quickload.config.Config;
import org.quickload.config.Task;
import org.quickload.config.TaskSource;
import org.quickload.config.ConfigSource;
import org.quickload.exec.BufferManager;
import org.quickload.plugin.PluginManager;
import org.quickload.record.Schema;
import org.quickload.spi.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class LocalFileInputPlugin
        extends FileInputPlugin
{
    @Inject
    public LocalFileInputPlugin(PluginManager pluginManager) {
        super(pluginManager);
    }

    // TODO consider when the page allocator object is released?

    public interface PluginTask
            extends Task
    {
        @Config("in:paths") // TODO temporarily added 'in:'
        @NotNull
        public List<String> getPaths();
    }

    @Override
    public TaskSource getFileInputTask(ProcConfig proc, ConfigSource config)
    {
        PluginTask task = config.loadTask(PluginTask.class);
        proc.setProcessorCount(task.getPaths().size());
        return config.dumpTask(task);
    }

    @Override
    public InputProcessor startFileInputProcessor(ProcTask proc,
            TaskSource taskSource, final int processorIndex, final BufferOperator next)
    {
        final PluginTask task = taskSource.loadTask(PluginTask.class);
        return ThreadInputProcessor.start(next, new Function<BufferOperator, ReportBuilder>() {
            public ReportBuilder apply(BufferOperator next) {
                return readFile(task, processorIndex, next);
            }
        });
    }

    public static ReportBuilder readFile(PluginTask task, int processorIndex, BufferOperator next)
    {
        // TODO ad-hoc
        String path = task.getPaths().get(processorIndex);

        try {
            File file = new File(path);
            byte[] bytes = new byte[(int) file.length()]; // TODO ad-hoc

            int len, offset = 0;
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                while ((len = in.read(bytes, offset, bytes.length - offset)) > 0) {
                    offset += len;
                }
                Buffer buffer = new Buffer(bytes);
                next.addBuffer(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO
        }

        return DynamicReport.builder(); // TODO
    }
}
