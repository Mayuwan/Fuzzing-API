package test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.*;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.SourceLocator;
import soot.options.Options;
import soot.*;

public class sootLoad {
	private String apiPath = "/home/myw/name";

	public void getClassUnderDir() {
		//apiClasses = new LinkedHashSet<String>();
		for (String clzName: SourceLocator.v().getClassesUnder(apiPath)) {
            System.out.printf("api class: %s\n", clzName);
			//加载要处理的类设置为应用类，并加载到soot环境Scene中  
            Scene.v().loadClass(clzName, SootClass.BODIES).setApplicationClass();
        }
	}
	
	
	private  void setOptions() {
		 soot.options.Options.v().set_keep_line_number(true);
		 soot.options.Options.v().set_whole_program(true);
       // LWG
		 soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");
		 soot.options.Options.v().setPhaseOption("cg", "verbose:false");
		 soot.options.Options.v().setPhaseOption("cg", "trim-clinit:true");
		 //soot.options.Options.v().setPhaseOption("jb.tr", "ignore-wrong-staticness:true");
		 		
		 soot.options.Options.v().set_src_prec(Options.src_prec_java);
		 soot.options.Options.v().set_prepend_classpath(true);
		 		
		 // don't optimize the program 
		 soot.options.Options.v().setPhaseOption("wjop", "enabled:false");
		 // allow for the absence of some classes
		 //soot.options.Options.v().set_allow_phantom_refs(true);
		 
	}
	
	private  void setSootClassPath() {
		StringBuffer cp = new StringBuffer();
		cp.append(".");
		cp.append(File.pathSeparator + apiPath);
	
	cp.append(File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/rt.jar"+File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/jce.jar");
		System.setProperty("soot.class.path", cp.toString());
	}
	
	
	public void getMethods() {
		for (SootClass clz : Scene.v().getApplicationClasses()) {
			System.out.println(Modifier.toString(clz.getModifiers())+" "+clz.getName());
			getFields(clz);
			if (clz.getMethods().size() == 0){System.out.println("do not have methods!!!!!");}
			else{
				System.out.println("method num:"+clz.getMethods().size());
				for(SootMethod me : clz.getMethods()) {
					System.out.println(me.toString());
					if (me.hasActiveBody()){
						System.out.println(me.getActiveBody().toString());
					}
				}
			}
			
		}
		
	}
	public void getFields(SootClass clz) {
		if (clz.getFields().size() == 0){System.out.println("do not have methods!!!!!");}
		else{
			System.out.println("field num:"+clz.getFields().size());
			for(SootField me : clz.getFields()) {
				System.out.println(Modifier.toString(me.getModifiers())+" "+me.toString());
				
				
			}
		}
		
	}
	public static void main(String[] args) {
		
		sootLoad s = new sootLoad();
		
		s.setSootClassPath();//设置classpath
		s.setOptions();//设置soot的选项
		
		s.getClassUnderDir();
		s.getMethods();
	}
}