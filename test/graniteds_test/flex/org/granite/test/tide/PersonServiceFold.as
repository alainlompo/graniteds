/**
 * Generated by Gas3 v1.1.0 (Granite Data Services) on Sat Jul 26 17:58:20 CEST 2008.
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERRIDDEN EACH TIME YOU USE
 * THE GENERATOR. CHANGE INSTEAD THE INHERITED CLASS (Person.as).
 */

package org.granite.test.tide {

    import flash.utils.flash_proxy;

    import org.granite.tide.Component;
    import org.granite.tide.ITideResponder;

    use namespace flash_proxy;


    [RemoteClass(alias="org.granite.test.tide.PersonService")]
    public class PersonServiceFold extends Component {
        
        public function PersonServiceFold():void {
            super();
        }

        [Lazy]
        public function modifyPerson(arg0:Person, resultHandler:Object = null, faultHandler:Function = null):void {
            if (faultHandler != null)
                callProperty("modifyPerson", arg0, resultHandler, faultHandler);
            else if (resultHandler is Function || resultHandler is ITideResponder)
                callProperty("modifyPerson", arg0, resultHandler);
            else if (resultHandler == null)
                callProperty("modifyPerson", arg0);
            else
                throw new Error("Illegal argument to remote call (last argument should be Function or ITideResponder): " + resultHandler);
        }
    }
}
