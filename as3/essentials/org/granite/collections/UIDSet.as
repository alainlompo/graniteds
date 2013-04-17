/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.collections {

	import mx.collections.IList;
	
    [RemoteClass(alias="org.granite.collections.UIDSet")]
    /**
     *	Serializable implementation of a set of UID elements
     *  
     * 	@author Franck WOLFF
     */
    public class UIDSet extends CheckedArrayCollection {

        override public function set source(s:Array):void {
            list = new UIDArraySet(s);
        }

        override public function set list(value:IList):void {
            if (value && !(value is UIDArraySet))
                value = new UIDArraySet(value.toArray());
            super.list = value;
        }
		
		override public function addAllAt(addList:IList, index:int):void { 
			var length:int = addList.length; 
			var position:int = 0; 
			for (var i:int = 0; i < length; i++) { 
				var oldLength:int = this.length; 
				this.addItemAt(addList.getItemAt(i), position+index); 
				if (oldLength < this.length) 
					position++;
			} 
		}
    }
}
