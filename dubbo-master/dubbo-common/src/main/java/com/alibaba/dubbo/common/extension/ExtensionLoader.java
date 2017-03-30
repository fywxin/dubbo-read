/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.common.extension;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.support.ActivateComparator;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.Holder;
import com.alibaba.dubbo.common.utils.StringUtils;

/**
 * Dubbo使用的扩展点获取。<p>
 * <ul>
 * <li>自动注入关联扩展点。</li>
 * <li>自动Wrap上扩展点的Wrap类。</li>
 * <li>缺省获得的的扩展点是一个Adaptive Instance。
 * </ul>
 * 
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">JDK5.0的自动发现机制实现</a>
 * 
 * @author william.liangf
 * @author ding.lid
 *
 * @see com.alibaba.dubbo.common.extension.SPI
 * @see com.alibaba.dubbo.common.extension.Adaptive
 * @see com.alibaba.dubbo.common.extension.Activate
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);
    
    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";
    
    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
    ///缓存所有的类加载器对象，一个接口对应一个类加载器，所有类加载器的objectFactory都是一样的
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();
    ///（类级别）获取类扩展点加载器
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)  throw new IllegalArgumentException("Extension type == null");
        if(!type.isInterface()) throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        if(!withExtensionAnnotation(type)) throw new IllegalArgumentException("Extension type(" + type + ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
      
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
        	//不同的类型对应不同的ExtensionLoader
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

    // ==============================

    ///扩展类接口
    private final Class<?> type;
    ///全局容器对象 包含SPI和Spring 容器，通过此对象可以查找所有系统被管理的对象， 主要是做IOC属性依赖注入的
    private final ExtensionFactory objectFactory;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();
    
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String,Class<?>>>();
    ///实现类与该类上的@Activate ， @Activate用于取值过滤排序
    private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();
    ///实现类无@Adaptive 则为 Xxx$Adaptive 代理类，否则为 有@Adaptive的实现类，eg：@AdaptiveExtensionFactory
    private volatile Class<?> cachedAdaptiveClass = null;

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();

    private String cachedDefaultName;

    private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();
    ///本接口的所有wrapper实现类
    private Set<Class<?>> cachedWrapperClasses;
    
    private volatile Throwable createAdaptiveInstanceError;
    
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();
    
    private ExtensionLoader(Class<?> type) {
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }
    
    /**
	 * 返回缺省的扩展，如果没有设置则返回<code>null</code>。 
	 */
	public T getDefaultExtension() {
	    getExtensionClasses();
        if(null == cachedDefaultName || cachedDefaultName.length() == 0 || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
	}

    /**
     * 返回指定名字的扩展。如果指定名字的扩展不存在，则抛异常 {@link IllegalStateException}.
     *
     * @param name
     * @return
     */
	@SuppressWarnings("unchecked")
	public T getExtension(String name) {
		if (name == null || name.length() == 0)
		    throw new IllegalArgumentException("Extension name == null");
		if ("true".equals(name)) {
		    return getDefaultExtension();
		}
		Holder<Object> holder = cachedInstances.get(name);
		if (holder == null) {
		    cachedInstances.putIfAbsent(name, new Holder<Object>());
		    holder = cachedInstances.get(name);
		}
		Object instance = holder.get();
		if (instance == null) {
		    synchronized (holder) {
	            instance = holder.get();
	            if (instance == null) {
	            	///根据名称 创建实例
	                instance = createExtension(name);
	                holder.set(instance);
	            }
	        }
		}
		return (T) instance;
	}
	
	@SuppressWarnings("unchecked")
    private T createExtension(String name) {
    	///clazz = name+T  eg DubboProtocol  dubbo  Protocol
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            ///入口 getExtension(name), 只会被调用一次，所以不会重复的 依赖注入和 wapper
            injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                	///(T) wrapperClass.getConstructor(type).newInstance(instance)  | new Wapper(instance)
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
    	//cachedAdaptiveInstance 实例对象保证类
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if(createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            }else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }
        return (T) instance;
    }
    
    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extenstion " + type + ", cause: " + e.getMessage(), e);
        }
    }
    
    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }
    
    private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        //System.out.println(code);
        ClassLoader classLoader = findClassLoader();
        com.alibaba.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }
    
    /**
     * 参考Spring AOP 动态代理
     * @return
     */
    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuidler = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for(Method m : methods) {
            if(m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // 完全没有Adaptive方法，则不需要生成Adaptive类
        if(! hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");
        
        codeBuidler.append("package " + type.getPackage().getName() + ";");
        codeBuidler.append("\nimport " + ExtensionLoader.class.getName() + ";");
        codeBuidler.append("\npublic class " + type.getSimpleName() + "$Adpative" + " implements " + type.getCanonicalName() + " {");
        
        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // 有类型为URL的参数
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");", urlTypeIndex);
                    code.append(s);
                    
                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex); 
                    code.append(s);
                    
                // 参数没有URL类型
                ///则从所有参数类的方法中查找 public URL getXxx() 的方法，查找到则将值取出设置到url变量中
                }else {
                    String attribMethod = null;
                    
                    // 找到参数的URL属性
                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            /// public URL getXxx()
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if(attribMethod == null) {
                        throw new IllegalStateException("fail to create adative class for interface " + type.getName()
                        		+ ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }
                    
                    // Null point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                                    urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                                    urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);

                    s = String.format("%s url = arg%d.%s();",URL.class.getName(), urlTypeIndex, attribMethod); 
                    code.append(s);
                }
                
                String[] value = adaptiveAnnotation.value();
                //没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                if(value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if(Character.isUpperCase(charArray[i])) {
                            if(i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        }else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[] {sb.toString()};
                }
                
                boolean hasInvocation = false;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].getName().equals("com.alibaba.dubbo.rpc.Invocation")) {
                        // Null Point check
                        String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
                        code.append(s);
                        s = String.format("\nString methodName = arg%d.getMethodName();", i); 
                        code.append(s);
                        hasInvocation = true;
                        break;
                    }
                }
                
                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                ///? 为啥要从后向前遍历，不断的替换getNameCode的值，而不直接采用第一个呢？？？？
                /// url.getParameter("client",url.getParameter("transporter", "netty"));
                for (int i = value.length - 1; i >= 0; --i) {
                	///最后一个
                    if(i == value.length - 1) {
                        if(null != defaultExtName) {
                            if(!"protocol".equals(value[i]))
                                if (hasInvocation) 
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                        } else {
                            if(!"protocol".equals(value[i]))
                                if (hasInvocation) 
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                            else
                                getNameCode = "url.getProtocol()";
                        }
                    ///非最后一个
                    }else {
                        if(!"protocol".equals(value[i]))
                            if (hasInvocation) 
                                getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                        else
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                    }
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                // check extName == null?
                String s = String.format("\nif(extName == null) " +
                		"throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);
                
                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);
                
                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }
            
            codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
            for (int i = 0; i < pts.length; i ++) {
                if (i > 0) {
                    codeBuidler.append(", ");
                }
                codeBuidler.append(pts[i].getCanonicalName());
                codeBuidler.append(" ");
                codeBuidler.append("arg" + i);
            }
            codeBuidler.append(")");
            if (ets.length > 0) {
                codeBuidler.append(" throws ");
                for (int i = 0; i < ets.length; i ++) {
                    if (i > 0) {
                        codeBuidler.append(", ");
                    }
                    codeBuidler.append(pts[i].getCanonicalName());
                }
            }
            codeBuidler.append(" {");
            codeBuidler.append(code.toString());
            codeBuidler.append("\n}");
        }
        codeBuidler.append("\n}");
        if (logger.isDebugEnabled()) {
            logger.debug(codeBuidler.toString());
        }
        return codeBuidler.toString();
    }
    
    private Class<?> getExtensionClass(String name) {
  	    if (type == null)
  	        throw new IllegalArgumentException("Extension type == null");
  	    if (name == null)
  	        throw new IllegalArgumentException("Extension name == null");
  	    Class<?> clazz = getExtensionClasses().get(name);
  	    if (clazz == null)
  	        throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
  	    return clazz;
  	}
  	
  	private Map<String, Class<?>> getExtensionClasses() {
          Map<String, Class<?>> classes = cachedClasses.get();
          if (classes == null) {
              synchronized (cachedClasses) {
                  classes = cachedClasses.get();
                  if (classes == null) {
                      classes = loadExtensionClasses();
                      cachedClasses.set(classes);
                  }
              }
          }
          return classes;
  	}

      // 此方法已经getExtensionClasses方法同步过。
      private Map<String, Class<?>> loadExtensionClasses() {
          final SPI defaultAnnotation = type.getAnnotation(SPI.class);
          if(defaultAnnotation != null) {
              String value = defaultAnnotation.value();
              if(value != null && (value = value.trim()).length() > 0) {
                  String[] names = NAME_SEPARATOR.split(value);
                  if(names.length > 1) {
                      throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                              + ": " + Arrays.toString(names));
                  }
                  if(names.length == 1) cachedDefaultName = names[0];
              }
          }
          
          Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
          loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
          loadFile(extensionClasses, DUBBO_DIRECTORY);
          loadFile(extensionClasses, SERVICES_DIRECTORY);
          return extensionClasses;
      }
      
      /***
       * 从配置文件中逐行提取类名，获取类路径，加载类，甄别类 类型，不同类型分别处理
       * 类型：（Adaptive，wrappers，普通类型，Activate ）
       * @param extensionClasses
       * @param dir
       */
      private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
          String fileName = dir + type.getName();
          try {
              Enumeration<java.net.URL> urls;
              ClassLoader classLoader = findClassLoader();
              if (classLoader != null) {
                  urls = classLoader.getResources(fileName);
              } else {
                  urls = ClassLoader.getSystemResources(fileName);
              }
              if (urls != null) {
                  while (urls.hasMoreElements()) {
                      java.net.URL url = urls.nextElement();
                      try {
                          BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                          try {
                              String line = null;
                              while ((line = reader.readLine()) != null) {
                              	//去注释
                                  final int ci = line.indexOf('#');
                                  if (ci >= 0) line = line.substring(0, ci);
                                  line = line.trim();
                                  if (line.length() > 0) {
                                      try {
                                          String name = null;
                                          int i = line.indexOf('=');
                                          if (i > 0) {
                                              name = line.substring(0, i).trim();
                                              line = line.substring(i + 1).trim();
                                          }
                                          if (line.length() > 0) {
                                              Class<?> clazz = Class.forName(line, true, classLoader);
                                              if (! type.isAssignableFrom(clazz)) {
                                                  throw new IllegalStateException("Error when load extension class(interface: " +
                                                          type + ", class line: " + clazz.getName() + "), class "  + clazz.getName() + "is not subtype of interface.");
                                              }
                                              if (clazz.isAnnotationPresent(Adaptive.class)) {
                                                  if(cachedAdaptiveClass == null) {
                                                      cachedAdaptiveClass = clazz;
                                                  } else if (! cachedAdaptiveClass.equals(clazz)) {
                                                      throw new IllegalStateException("More than 1 adaptive class found: "
                                                              + cachedAdaptiveClass.getClass().getName() + ", " + clazz.getClass().getName());
                                                  }
                                              } else {
                                                  try {
                                                      clazz.getConstructor(type);
                                                      Set<Class<?>> wrappers = cachedWrapperClasses;
                                                      if (wrappers == null) {
                                                          cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                                                          wrappers = cachedWrapperClasses;
                                                      }
                                                      wrappers.add(clazz);
                                                  } catch (NoSuchMethodException e) {
                                                      clazz.getConstructor();
                                                      if (name == null || name.length() == 0) {
                                                          name = findAnnotationName(clazz);
                                                          if (name == null || name.length() == 0) {
                                                              if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                      && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                                  name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                              } else {
                                                                  throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                                              }
                                                          }
                                                      }
                                                      String[] names = NAME_SEPARATOR.split(name);
                                                      if (names != null && names.length > 0) {
                                                          Activate activate = clazz.getAnnotation(Activate.class);
                                                          if (activate != null) {
                                                              cachedActivates.put(names[0], activate);
                                                          }
                                                          for (String n : names) {
                                                              if (! cachedNames.containsKey(clazz)) {
                                                                  cachedNames.put(clazz, n);
                                                              }
                                                              Class<?> c = extensionClasses.get(n);
                                                              if (c == null) {
                                                                  extensionClasses.put(n, clazz);
                                                              } else if (c != clazz) {
                                                                  throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                              }
                                                          }
                                                      }
                                                  }
                                              }
                                          }
                                      } catch (Throwable t) {
                                          IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                          exceptions.put(line, e);
                                      }
                                  }
                              } // end of while read lines
                          } finally {
                              reader.close();
                          }
                      } catch (Throwable t) {
                          logger.error("Exception when load extension class(interface: " +
                                              type + ", class file: " + url + ") in " + url, t);
                      }
                  } // end of while urls
              }
          } catch (Throwable t) {
              logger.error("Exception when load extension class(interface: " +
                      type + ", description file: " + fileName + ").", t);
          }
      }
    
    /***
     * 依赖注入： 使用set(P) 方法注入  eg:setName(String name);
     * 注入规则： 1.由set方法提取属性名称  eg：name
     * 			2.从ExtensionFactory 中根据名称获取依赖对象， 递归循环获取
     * @param instance
     * @return
     */
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    private static ClassLoader findClassLoader() {
        return  ExtensionLoader.class.getClassLoader();
    }
    
    public List<T> getActivateExtension(URL url, String key) {
        return getActivateExtension(url, key, null);
    }
    public List<T> getActivateExtension(URL url, String key, String group) {
        String value = url.getParameter(key);
        return getActivateExtension(url, value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
    }
    public List<T> getActivateExtension(URL url, String[] values) {
        return getActivateExtension(url, values, null);
    }
    
    /****
     * Get activate extensions.
     * 服务提供方和服务消费方调用过程拦截，Dubbo本身的大多功能均基于此扩展点实现，每次远程方法执行，该拦截都会被执行，请注意对性能的影响。
			约定：
			
			用户自定义filter默认在内置filter之后。
			特殊值default，表示缺省扩展点插入的位置。
			比如：filter="xxx,default,yyy"，表示xxx在缺省filter之前，yyy在缺省filter之后。
			特殊符号-，表示剔除。
			比如：filter="-foo1"，剔除添加缺省扩展点foo1。
			比如：filter="-default"，剔除添加所有缺省扩展点。
			provider和service同时配置的filter时，累加所有filter，而不是覆盖。
			比如：<dubbo:provider filter="xxx,yyy"/>和<dubbo:service filter="aaa,bbb" />，则xxx,yyy,aaa,bbb均会生效。
			如果要覆盖，需配置：<dubbo:service filter="-xxx,-yyy,aaa,bbb" />
     * @see com.alibaba.dubbo.common.extension.Activate
     * @param url url
     * @param values extension point names 补偿
     * @param group group 过滤
     * @return extension list which are activated
     */
    public List<T> getActivateExtension(URL url, String[] values, String group) {
        List<T> exts = new ArrayList<T>();
        ///
        List<String> names = values == null ? new ArrayList<String>(0) : Arrays.asList(values);
        ///group过滤后的集合在排除在 values 里面的值（会在下一个循环中补偿回来），目的是group按正常排序，values 全部驾到group的尾部
        if (! names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {//-default
            getExtensionClasses();
            for (Map.Entry<String, Activate> entry : cachedActivates.entrySet()) {
                String name = entry.getKey();
                Activate activate = entry.getValue();
                if (isMatchGroup(group, activate.group())) {
                    T ext = getExtension(name);
                    if (! names.contains(name) && ! names.contains(Constants.REMOVE_VALUE_PREFIX + name) && isActive(activate, url)) {
                        exts.add(ext);
                    }
                }
            }
            Collections.sort(exts, ActivateComparator.COMPARATOR);
        }
        
        ///往group过滤后的集合中补充values值
        ///eg: names-> ["a", "b", "-c", "default", "-b", "b", "e"]   exts-> [C, D]
        ///usrs-> [a,C,D,e]  //default 用于定位调节顺序
        List<T> usrs = new ArrayList<T>();
        for (int i = 0; i < names.size(); i ++) {
        	String name = names.get(i);
            if (! name.startsWith(Constants.REMOVE_VALUE_PREFIX) && ! names.contains(Constants.REMOVE_VALUE_PREFIX + name)) {
            	if (Constants.DEFAULT_KEY.equals(name)) {
            		if (usrs.size() > 0) {
	            		exts.addAll(0, usrs);
	            		usrs.clear();
            		}
            	} else {
	            	T ext = getExtension(name);
	            	usrs.add(ext);
            	}
            }
        }
        if (usrs.size() > 0) {
        	exts.addAll(usrs);
        }
        ///最后并未再进行排序
        return exts;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }
    
    public String getExtensionName(T extensionInstance) {
        return getExtensionName(extensionInstance.getClass());
    }

    public String getExtensionName(Class<?> extensionClass) {
        return cachedNames.get(extensionClass);
    }
    
    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);


        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if(i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i ++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }
    
    /**
	 * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。 
	 */
	public String getDefaultExtensionName() {
	    getExtensionClasses();
	    return cachedDefaultName;
	}
    
    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
    }
    
    /**
     * 返回已经加载的扩展点的名字。
     * <p />
     * 一般应该调用{@link #getSupportedExtensions()}方法获得扩展，这个方法会返回所有的扩展点。
     *
     * @see #getSupportedExtensions()
     */
    public Set<String> getLoadedExtensions() {
        return Collections.unmodifiableSet(new TreeSet<String>(cachedInstances.keySet()));
    }
    
    /**
     * 返回扩展点实例，如果没有指定的扩展点或是还没加载（即实例化）则返回<code>null</code>。注意：此方法不会触发扩展点的加载。
     * <p />
     * 一般应该调用{@link #getExtension(String)}方法获得扩展，这个方法会触发扩展点加载。
     *
     * @see #getExtension(String)
     */
    @SuppressWarnings("unchecked")
    public T getLoadedExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        return (T) holder.get();
    }
    
    public boolean hasExtension(String name) {
	    if (name == null || name.length() == 0)
	        throw new IllegalArgumentException("Extension name == null");
	    try {
	        return getExtensionClass(name) != null;
	    } catch (Throwable t) {
	        return false;
	    }
	}
    
    private boolean isMatchGroup(String group, String[] groups) {
        if (group == null || group.length() == 0) {
            return true;
        }
        if (groups != null && groups.length > 0) {
            for (String g : groups) {
                if (group.equals(g)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isActive(Activate activate, URL url) {
        String[] keys = activate.value();
        if (keys == null || keys.length == 0) {
            return true;
        }
        for (String key : keys) {
            for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if ((k.equals(key) || k.endsWith("." + key)) && ConfigUtils.isNotEmpty(v)) {
                    return true;
                }
            }
        }
        return false;
    }
      
      @SuppressWarnings("deprecation")
      private String findAnnotationName(Class<?> clazz) {
          com.alibaba.dubbo.common.Extension extension = clazz.getAnnotation(com.alibaba.dubbo.common.Extension.class);
          if (extension == null) {
              String name = clazz.getSimpleName();
              if (name.endsWith(type.getSimpleName())) {
                  name = name.substring(0, name.length() - type.getSimpleName().length());
              }
              return name.toLowerCase();
          }
          return extension.value();
      }
      
      
      /**
       * 编程方式添加新扩展点。
       *
       * @param name 扩展点名
       * @param clazz 扩展点类
       * @throws IllegalStateException 要添加扩展点名已经存在。
       */
      public void addExtension(String name, Class<?> clazz) {
          getExtensionClasses(); // load classes

          if(!type.isAssignableFrom(clazz)) {
              throw new IllegalStateException("Input type " +
                      clazz + "not implement Extension " + type);
          }
          if(clazz.isInterface()) {
              throw new IllegalStateException("Input type " +
                      clazz + "can not be interface!");
          }

          if(!clazz.isAnnotationPresent(Adaptive.class)) {
              if(StringUtils.isBlank(name)) {
                  throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
              }
              if(cachedClasses.get().containsKey(name)) {
                  throw new IllegalStateException("Extension name " +name + " already existed(Extension " + type + ")!");
              }

              cachedNames.put(clazz, name);
              cachedClasses.get().put(name, clazz);
          }else {
              if(cachedAdaptiveClass != null) {
                  throw new IllegalStateException("Adaptive Extension already existed(Extension " + type + ")!");
              }

              cachedAdaptiveClass = clazz;
          }
      }

      /**
       * 编程方式添加替换已有扩展点。
       *
       * @param name 扩展点名
       * @param clazz 扩展点类
       * @throws IllegalStateException 要添加扩展点名已经存在。
       * @deprecated 不推荐应用使用，一般只在测试时可以使用
       */
      @Deprecated
      public void replaceExtension(String name, Class<?> clazz) {
          getExtensionClasses(); // load classes

          if(!type.isAssignableFrom(clazz)) {
              throw new IllegalStateException("Input type " +
                      clazz + "not implement Extension " + type);
          }
          if(clazz.isInterface()) {
              throw new IllegalStateException("Input type " +
                      clazz + "can not be interface!");
          }

          if(!clazz.isAnnotationPresent(Adaptive.class)) {
              if(StringUtils.isBlank(name)) {
                  throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
              }
              if(!cachedClasses.get().containsKey(name)) {
                  throw new IllegalStateException("Extension name " +
                          name + " not existed(Extension " + type + ")!");
              }

              cachedNames.put(clazz, name);
              cachedClasses.get().put(name, clazz);
              cachedInstances.remove(name);
          }else {
              if(cachedAdaptiveClass == null) {
                  throw new IllegalStateException("Adaptive Extension not existed(Extension " + type + ")!");
              }

              cachedAdaptiveClass = clazz;
              cachedAdaptiveInstance.set(null);
          }
      }
      
      private static <T> boolean withExtensionAnnotation(Class<T> type) {
          return type.isAnnotationPresent(SPI.class);
      }
}