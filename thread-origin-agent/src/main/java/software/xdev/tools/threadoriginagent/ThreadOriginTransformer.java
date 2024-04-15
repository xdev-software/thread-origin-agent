/*
 * Copyright © 2019 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.tools.threadoriginagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;


/**
 * This javaagent should ALWAYS be the first!
 */
public class ThreadOriginTransformer implements ClassFileTransformer
{
	private static final String PROCEED = "$proceed($$); ";
	
	private static final String PRINT_STACK =
		"java.lang.StackTraceElement[] elements = java.lang.Thread.currentThread().getStackTrace(); "
			+ "java.lang.StringBuilder sb = new java.lang.StringBuilder(); "
			+ "for(int i = 1; i < elements.length; i++) "
			+ "   sb.append(\"[TOA] \\t\")"
			+ "      .append(elements[i])"
			+ "      .append(i < elements.length - 1 ? java.lang.System.lineSeparator() : \"\"); "
			+ "java.lang.System.out.println(sb.toString()); ";
	
	/**
	 * Don't log calls to classnames if they start with the string mentioned here<br/> e.g:
	 * -javaagent:thread-origin-agent-1.0.0.jar=sun/awt,sun/java2d
	 */
	private final List<String> excluded = new ArrayList<>();
	
	public ThreadOriginTransformer(final String argument)
	{
		super();
		
		log("Arg: " + argument);
		
		if(argument != null)
		{
			this.excluded.addAll(Arrays.asList(argument.split(",")));
		}
		
		log("Ignoring excluded: " + String.join(",", this.excluded));
	}
	
	/**
	 * Add agent
	 * <p>
	 * see also <code>src/main/resources/META-INF/MANIFEST.MF</code>
	 * </p>
	 */
	public static void premain(final String agentArgument, final Instrumentation instrumentation)
	{
		instrumentation.addTransformer(new ThreadOriginTransformer(agentArgument));
		
		log("Trying to retransform loaded classes");
		long unmodifiable = 0;
		long success = 0;
		for(final Class<?> loadedClazz : instrumentation.getAllLoadedClasses())
		{
			if(loadedClazz.getName().startsWith("software.xdev.tools")
				|| loadedClazz.getName().startsWith("javassist"))
			{
				log("Ignoring " + loadedClazz.getName());
				continue;
			}
			
			try
			{
				instrumentation.retransformClasses(loadedClazz);
				success++;
			}
			catch(final UnmodifiableClassException e)
			{
				unmodifiable++;
			}
		}
		log("Retransformed loaded classes; " + success + "x successful, " + unmodifiable + "x unmodifiable");
	}
	
	@Override
	public byte[] transform(
		final ClassLoader loader,
		final String className,
		final Class<?> clazz,
		final java.security.ProtectionDomain domain,
		final byte[] bytes)
	{
		byte[] resultingBytes = bytes;
		if(className == null)
		{
			log("ClassName was null; Class=" + clazz.getCanonicalName());
			return resultingBytes;
		}
		
		if(this.excluded.stream().anyMatch(className::startsWith))
		{
			log("Excluded class=" + className);
			return resultingBytes;
		}
		
		try
		{
			
			final ClassPool classPool = ClassPool.getDefault();
			final CtClass classUnderTransformation = classPool.makeClass(new java.io.ByteArrayInputStream(bytes));
			
			if(classUnderTransformation == null)
			{
				return resultingBytes;
			}
			
			classUnderTransformation.instrument(new ExprEditor()
			{
				
				@Override
				public void edit(final MethodCall m) throws CannotCompileException
				{
					final CtMethod method;
					try
					{
						method = m.getMethod();
					}
					catch(final NotFoundException e)
					{
						// log("Could not find method '" + m.getSignature() + "':" + e.getMessage());
						return;
					}
					
					final String classname = method.getDeclaringClass().getName();
					final String methodName = method.getName();
					
					ThreadOriginTransformer.this.replaceThread(classname, m, methodName);
				}
			});
			
			resultingBytes = classUnderTransformation.toBytecode();
		}
		catch(final Exception e)
		{
			log("Could not instrument "
				+ className
				+ "/"
				+ clazz.getCanonicalName()
				+ ", exception: "
				+ e.getMessage());
		}
		
		return resultingBytes;
	}
	
	void replaceThread(final String classname, final MethodCall m, final String methodName)
		throws CannotCompileException
	{
		if(!Thread.class.getName().equals(classname))
		{
			return;
		}
		
		if(methodName.equals("start"))
		{
			m.replace("{ "
				+ "System.out.println(\"[TOA] Detected Thread.start() id: \" + ((Thread)$0).getId() + \" name: \" + "
				+ "((Thread)$0).getName()); "
				+ PRINT_STACK
				+ PROCEED
				+ "} ");
		}
		else if(methodName.equals("join"))
		{
			m.replace("{ "
				+ "System.out.println(\"[TOA] Detected Thread.join() id: \" + ((Thread)$0).getId() + \" name: \" + "
				+ "((Thread)$0).getName()); "
				+ PROCEED
				+ "} ");
		}
	}
	
	@SuppressWarnings("java:S106")
	private static void log(final String message)
	{
		System.out.println("[TOA] " + message);
	}
}
