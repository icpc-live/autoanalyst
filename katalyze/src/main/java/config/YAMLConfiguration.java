/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The names of the contributors may not be used to endorse or promote
 *  products derived from this software without specific prior written
 * permission.
 */


/* This file is originally from https://github.com/mbredel/configurations-yaml/blob/master/src/main/java/com/github/mbredel/commons/configuration/YAMLConfiguration.java
   and has only been carefully modified in order to compile.
 */

package config;

import org.apache.commons.configuration.AbstractHierarchicalFileConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The YAML configuration implements the hierarchical file configuration
 * for YAML (and also JSON) files. It uses the snakeYAML library to
 * read and parse YAML files. It stores the configuration information
 * in Commons-Configuration configuration nodes.
 *
 * @author Michael Bredel
 */
public class YAMLConfiguration extends AbstractHierarchicalFileConfiguration
{
    /** The serial version UID. */
    private static final long serialVersionUID = 2453781111653383551L;

    /** Constant for the default root element name. */
    private static final String DEFAULT_ROOT_NAME = "configuration";

    /**
     * Creates a new instance of {@code YAMLConfiguration}.
     */
    public YAMLConfiguration()
    {
        super();
        setLogger(LogFactory.getLog(YAMLConfiguration.class));
    }

    /**
     * Creates a new instance of {@code YAMLConfiguration} and copies the
     * content of the passed in configuration into this object. Note that only
     * the data of the passed in configuration will be copied. If, for instance,
     * the other configuration is a {@code YAMLConfiguration}, too,
     * things like comments or processing instructions will be lost.
     *
     * @param c the configuration to copy
     */
    @SuppressWarnings("unused")
    public YAMLConfiguration(HierarchicalConfiguration c)
    {
        super(c);
        clearReferences(getRootNode());
        //setRootElementName(getRootNode().getName());
        setLogger(LogFactory.getLog(YAMLConfiguration.class));
    }

    /**
     * Creates a new instance of{@code YAMLConfiguration}. The
     * configuration is loaded from the specified file
     *
     * @param fileName the name of the file to load
     * @throws ConfigurationException if the file cannot be loaded
     */
    @SuppressWarnings("unused")
    public YAMLConfiguration(String fileName) throws ConfigurationException
    {
        super(fileName);
        setLogger(LogFactory.getLog(YAMLConfiguration.class));
    }

    /**
     * Creates a new instance of {@code YAMLConfiguration}.
     * The configuration is loaded from the specified file.
     *
     * @param file the file
     * @throws ConfigurationException if an error occurs while loading the file
     */
    @SuppressWarnings("unused")
    public YAMLConfiguration(File file) throws ConfigurationException
    {
        super(file);
        setLogger(LogFactory.getLog(YAMLConfiguration.class));
    }

    /**
     * Creates a new instance of {@code YAMLConfiguration}.
     * The configuration is loaded from the specified URL.
     *
     * @param url the URL
     * @throws ConfigurationException if loading causes an error
     */
    @SuppressWarnings("unused")
    public YAMLConfiguration(URL url) throws ConfigurationException
    {
        super(url);
        setLogger(LogFactory.getLog(YAMLConfiguration.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Reader in) throws ConfigurationException
    {
        try
        {
            Yaml yaml = new Yaml();
            Map<String, Object> map = (Map) yaml.load(in);
            // Construct the configuration tree.
            ConfigurationNode configurationNode = getRootNode();
            configurationNode.setName(DEFAULT_ROOT_NAME);
            constructHierarchy(configurationNode, map);
        }
        catch (ClassCastException e)
        {
            throw new ConfigurationException("Error parsing", e);
        }
        catch (Exception e)
        {
            this.getLogger().debug("Unable to load the configuration", e);
            throw new ConfigurationException("Unable to load the configuration", e);
        }
    }

    /**
     * Loads the configuration from the given input stream.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     */
    @Override
    @SuppressWarnings("unchecked")
    public void load(InputStream in) throws ConfigurationException
    {
        try
        {
            Yaml yaml = new Yaml();
            Map<String, Object> map = (Map) yaml.load(in);
            // Construct the configuration tree.
            ConfigurationNode configurationNode = getRootNode();
            configurationNode.setName(DEFAULT_ROOT_NAME);
            constructHierarchy(configurationNode, map);
        }
        catch (ClassCastException e)
        {
            throw new ConfigurationException("Error parsing", e);
        }
        catch (Exception e)
        {
            this.getLogger().debug("Unable to load the configuration", e);
            throw new ConfigurationException("Unable to load the configuration", e);
        }
    }

    /**
     * Constructs the internal configuration nodes hierarchy.
     *
     * @param node The configuration node that is the root of the current configuration section.
     * @param map The map with the yaml configurations nodes, i.e. String -> Object.
     */
    @SuppressWarnings("unchecked")
    private void constructHierarchy(ConfigurationNode node, Map<String, Object> map)
    {
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map)
            {
                ConfigurationNode treeNode = createNode(key);
                constructHierarchy(treeNode, (Map) value);
                node.addChild(treeNode);
            }
            else
            {
                ConfigurationNode leaveNode = createNode(key);
                leaveNode.setValue(value);
                node.addChild(leaveNode);
            }
        }
    }

    @Override
    public void save(Writer writer) throws ConfigurationException
    {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        String output = yaml.dump(constructMap(getRootNode()));
        System.out.println(output);
    }

    /**
     * Constructs a YAML map, i.e. String -> Object from a given
     * configuration node.
     *
     * @param node The configuration node to create a map from.
     * @return A Map that contains the configuration node information.
     */
    public Map<String, Object> constructMap(ConfigurationNode node)
    {
        Map<String, Object> map = new HashMap<>(node.getChildrenCount());
        for (ConfigurationNode cNode : node.getChildren())
        {
            if (cNode.getChildren().isEmpty())
            {
                map.put(cNode.getName(), cNode.getValue());
            }
            else
            {
                map.put(cNode.getName(), constructMap(cNode));
            }
        }
        return map;
    }

    /**
     * Creates a copy of this object. The new configuration object will contain
     * the same properties as the original, but it will lose any connection to a
     * source document (if one exists). This is to avoid race conditions if both
     * the original and the copy are modified and then saved.
     *
     * @return the copy
     */
    @Override
    public Object clone()
    {
        YAMLConfiguration copy = (YAMLConfiguration) super.clone();

        // clear document related properties
        copy.setDelegate(copy.createDelegate());
        // clear all references in the nodes, too
        clearReferences(copy.getRootNode());

        return copy;
    }

    /**
     * Creates the file configuration delegate for this object. This implementation
     * will return an instance of a class derived from {@code FileConfigurationDelegate}
     * that deals with some specialties of {@code YAMLConfiguration}.
     * @return the delegate for this object
     */
    @Override
    protected FileConfigurationDelegate createDelegate()
    {
        return new YAMLFileConfigurationDelegate();
    }

    /**
     * A special implementation of the {@code FileConfiguration} interface that is
     * used internally to implement the {@code FileConfiguration} methods
     * for {@code YAMLConfiguration}, too.
     */
    private class YAMLFileConfigurationDelegate extends FileConfigurationDelegate
    {
        @Override
        public void load(InputStream in) throws ConfigurationException
        {
            YAMLConfiguration.this.load(in);
        }
    }
}