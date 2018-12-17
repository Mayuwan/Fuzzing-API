package subClassTemplate;
import java.io.File;
/**使用soot加载问一个Class中的所有Field和Methods，不包含父类的方法和
 * 1.设置soot选项以及类加载路径
 * 2.设置应用类
 * 3.使用soot加载应用类，分析sootClass
 * */
import java.lang.reflect.Modifier;
import java.util.*;

import soot.*;
import soot.options.Options;
public class SootLoad {
		//private Parse parse;
	    private String applicatoinClassPath;/**要加载应用类的路径 * */
		private Set<String> apiClasses;/**api所在的类的集合*/
		
		public SootLoad(String filePath){
			applicatoinClassPath = filePath;
		}
	
		/**
		 *配置soot选项*/
		private void setOptions() {
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
			 soot.options.Options.v().set_allow_phantom_refs(true);
			 
			 
		}
		/**
		 * 设置soot类路径*/
		private void setSootClassPath() {
			StringBuffer cp = new StringBuffer();
			cp.append(".");
			//cp.append(File.pathSeparator + applicatoinClassPath);///自己写的类需要添加
			//add the api modeling directory first so we can load modeling classes
			//cp.append("/media/myw/Study/Master/Research/DroidSafe/droidsafe-src-master/android-lib/droidsafe-api-model.jar");
			//cp.append(File.pathSeparator+"/media/myw/Study/Master/Research/DroidSafe/droidsafe-src-master/bin/droidsafe-core.jar");
			cp.append(File.pathSeparator+"/home/myw/Android/Sdk/platforms/android-26/android.jar");
			cp.append(File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/rt.jar"+File.pathSeparator+"/usr/lib/jvm/jdk1.7.0_79/jre/lib/jce.jar");
			//System.out.println("soot.class.path:\n"+cp.toString());
			
	        System.setProperty("soot.class.path", cp.toString());
		}
		
		/**
		 * 设置api文件所在目录路径
		 * @param apiPath api文件所在目录路径
		 * */
		private void setApplicatonClasses(){
			apiClasses = new LinkedHashSet<String>();
			for (String clzName: SourceLocator.v().getClassesUnder(applicatoinClassPath)) {
	            System.out.printf("api class: %s\n", clzName);
	            apiClasses.add(clzName);
	            //Scene.v().loadClass(clzName, SootClass.BODIES).setApplicationClass();
	        }
			//loadAppClasses(apiClasses,false);
		}
		public Set<String> getapiClasses() {
	        return apiClasses;
		}
		/**使用soot加载所有的class
		 * */
		public  Set<String> loadAppClasses(boolean shouldReplace) {
		    Set<String> notLoaded = new HashSet<String>();
			//load the application classes and set them as app classes
			for (String clz : apiClasses) {
			    //if we don't want to replace and this class is already defined, then skip
			    if (!shouldReplace && Scene.v().containsClass(clz) && Scene.v().getSootClass(clz).isApplicationClass()) {
			    	//System.out.printf("Not loading %s, already loaded\n", clz);
			        notLoaded.add(clz);
			        continue;
			    }
				//Scene.v().loadClassAndSupport(clz).setApplicationClass();
				Scene.v().loadClass(clz, SootClass.BODIES).setApplicationClass();;
				//System.out.printf("Loading class as application class: %s", clz);
			}
			System.out.printf("not loaded: %d\n", notLoaded.size());
			return notLoaded;
		}
	
		public void printFields(SootClass myClass){
			System.out.println();
			for (SootMethod mth : myClass.getMethods()){
				 System.out.printf("%s :%s\n",Modifier.toString(mth.getModifiers()),mth.getName());
			}
	        for(SootField fld : myClass.getFields()){
	           System.out.printf("%s :%s\n",Modifier.toString(fld.getModifiers()),fld.getName());
	            
	        }
			System.out.println();
	    }
		/**对soot中的所有app class写模板文件
		 * @param fileName 所有api方法中的引用类型txt文件路径
		 * */
		public void readMethods(){
			//Parse parse = new Parse(filePath);
			//parse.read();
			
			int n = 0;
			int num = 0;
			int no = 0;
			for (SootClass clz : Scene.v().getApplicationClasses()) {
				if(clz != null) n++;
				String className = clz.getName();
				System.out.printf("fieldNum:%d  MethodNum:%d\n",clz.getFields().size(),clz.getMethods().size());
				
				//AppendContentToFile.method3("/media/myw/Study/Master/Research/DroidSafe/运行日志/allClasses", className);
				//if(parse.getAllClassName().contains(className)){
				if(Modifier.isInterface(clz.getModifiers())){
					System.out.printf("%s是接口，必须实现该接口，不能重写\n",className);
					continue;
				}
				if(Modifier.isAbstract(clz.getModifiers())){
					System.out.printf("%s是Abstract类，必须重写方法，不能重写\n",className);
					continue;
				}
				int i=0;
				 for(SootField fld : clz.getFields()){
					 if(Modifier.isFinal( fld.getModifiers())){i++;}
				 }
				 if(Modifier.isFinal( clz.getModifiers()) && (i != 0 && i == clz.getFields().size())){
					 System.out.printf("%s是immutable类\n",className);
					AppendContentToFile.append("/media/myw/Study/Master/Research/DroidSafe/script/newImmutableType.txt",className);
					continue;
				}
				if(Modifier.isFinal( clz.getModifiers())){
					System.out.printf("%s是final类，不可写子类\n",className);
					continue;
				}
				
				Template te = new Template("overidedSubclass", clz);
				System.out.printf("original class:%s\n",clz.getName());
				if(te.hasAccessibleConstructor()){
					Parse.save(te.toString(),System.getProperty("user.dir")+"/Proxys/"+te.getClassName());
					System.out.printf("%s已写模板\n",className);
				}
				
				else{
					System.out.printf("%s没有可访问的构造方法\n",className);
				}
					
				//}
				
				
				/*if (clz.getMethods().size() == 0) {no += 1;	}
				else{
					//System.out.println(className);
					//AppendContentToFile.append("/media/myw/Study/Master/Research/DroidSafe/运行日志/methods", className);
					for (SootMethod method : clz.getMethods()) {
						 //String modifier = Modifier.toString(method.getModifiers());
						
						String sig = method.getSignature(clz, method.getName(), method.getParameterTypes(), method.getReturnType());
						//AppendContentToFile.method3("/media/myw/Study/Master/Research/DroidSafe/运行日志/methods", sig);
						if((method.isPublic() || method.isProtected()) && method.isNative()){
							 String sig = method.getSignature(clz, method.getName(), method.getParameterTypes(), method.getReturnType());
							 //if(nativeMethod.contains(sig)){ 
								 //s.add(className);
								 num += 1;
								 //AppendContentToFile.method3("/media/myw/Study/Master/Research/DroidSafe/运行日志/canUseMth", sig);
							
							 //} 
						 }
				}*/
				
			}
			System.out.println("the sum iof classes that Do ot have any methods: "+ no);
			System.out.println("application class num: "+ n);
			/*for (String sxda : s){
				if (d.contains(sxda)){
					d.remove(sxda);
				}
			}
			//System.out.printf("api directory contain %d classes\n",apiClasses.size());
			for (String reviewed : s) {
				apiClasses.remove(reviewed);
	        }*/
			//System.out.println(nativeMethod.size());
			//System.out.println("native classes that have been reviewed:"+ d.size());//183
			//System.out.println("all classes that native.txt contain classes:"+s.size());//47
			System.out.printf("not private native method number: %d\n", num);
			
		}
		
		public Set<String> getApiClasses(){return apiClasses;}
		
		
		public static void main(String[] arg){
			soot.G.reset();
		
			SootLoad s = new SootLoad("/home/myw/name");//test path:/home/myw/name
			
			s.setSootClassPath();
			s.setOptions();
			//Scene.v().loadNecessaryClasses();
			s.setApplicatonClasses();
			s.loadAppClasses(true);
			System.out.printf("api directory contain %d classes\n",s.getApiClasses().size());
			s.readMethods();
		}
}
