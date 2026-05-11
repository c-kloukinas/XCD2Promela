#ifndef PACKAGE
#define PACKAGE 1

//#include "TYPE_ID.h"
//#include "TYPE_Action.h"
//#include "TYPE_Amount.h"

/******Constants*********/
#define RESULT 0
#define EXCEPTION 1
#define bit_MIN 0
#define bit_MAX 1
#define bool_MIN 0
#define bool_MAX 1
#define byte_MIN 0
#define byte_MAX 255
#define mtype_MIN 0
#define mtype_MAX 255

/********Macros for Conditional Expressions*********/
//// macro for returning AND of expressions
#define AND(a, b) ((a) && (b))
//// macro for returning OR of expressions
#define OR(a, b) ((a) || (b))
//// macro for returning IMPLIES
#define IMPLIES(a, b) (!(a) || (b))

/************Macro for naming convention********/
#define Component_i_smt(component,i,smt) Component_i_2_smt(component,i, smt)
#define Component_i_2_smt(component,i,smt) COMPONENT_##component##_##i ## smt

#define Component_i_smt_ins(component,compIns,i, smt) Component_i_2_smt_ins(component,compIns,i, smt)
#define Component_i_2_smt_ins(component,compIns, i,smt) COMPONENT_##component##_##compIns##_##i ## smt

#define SubComponent_i_smt(compositeName,compositeIndex, component,compIns, i,smt) \
           SubComponent_i_2_smt(compositeName,compositeIndex,component,compIns,i, smt)
#define SubComponent_i_2_smt(compositeName,compositeIndex,component,compIns, i,smt) compositeName##_##compositeIndex##_COMPONENT_##component##_##compIns##_##i ## smt

#define SubConnector_i_smt(compositeName,compositeIndex, connector,connIns_i,smt) \
           SubConnector_i_2_smt(compositeName,compositeIndex,connector,connIns_i, smt)
#define SubConnector_i_2_smt(compositeName,compositeIndex,connector,connIns_i,smt) compositeName##_##compositeIndex##_CONNECTOR_##connector##_##connIns_i ## smt

#define Component_i_smt_pre_post(pre, component,i, smt)  Component_i_2_pre_post(pre,component,i, smt)
#define Component_i_2_pre_post(pre,component,i,smt) pre##COMPONENT_##component##_##i ## smt
/*******Macros for VARIABLE Operations************/
//macro for returning process names for component instances
#define instance_name(CompositeName,CompositeID,SubCompType,SubCompIns, Instance) \
             instance_name2(CompositeName,CompositeID,SubCompType, SubCompIns, Instance)
#define instance_name2(CompositeName,CompositeID,SubCompType,SubCompIns, Instance) \
                COMPONENT_TYPE_##CompositeName##_##CompositeID##_SUBCOMPONENT_## SubCompType##_##SubCompIns##_##Instance
// macro for returning the minimum value of a type
#define Min_of_Type(Type) Type##_MIN
// macro for returning the maximum value of a type
#define Max_of_Type(Type) Type##_MAX
// macro for returning the minimum value for a variable
#define Min_of_Var(Var) Min_of_Var2(TYPEOF_##Var)
#define Min_of_Var2(Type) Min_of_Type(Type)
// macro for returning the minimum value for a variable
#define Max_of_Var(Var) Max_of_Var2(TYPEOF_##Var)
#define Max_of_Var2(Type) Max_of_Type(Type)
//macro for returning the variable name that holds a result
#define Result_var(ComponentType, port, method) COMPONENT_##ComponentType##_VAR_PORT_##port##_METHOD_##method##_RESULT
#define TEMP(X) temp_##X
//macro for returning the variable name that holds a post value of a variable
#define POST(X) post_##X
//macro for returning the constant for a variable type
#define TypeOf(X) TYPEOF_##X
//macro for returning the constant for an initial value of a variable
#define InitialValue(X) INITIALVALUE_##X
//macro for returning the constant for an initial post value of a variable
#define InitialValue_post(X) InitialValue_post_##X
//macro for returning variable name
#define Variable_Name(compType, variable) COMPONENT_##compType##_VAR_##variable
//macro for returning refined contract id
#define refined_contract_request(compositeName, compositeIndex, comptype, compIns, Instance, portName,action) \
SubComponent_i_smt(compositeName,compositeIndex,comptype,compIns, Instance, _PORT_##portName##_##ACTION_##action##_REFINED_ICONTRACT_REQUEST)

#define refined_contract_response(compositeName, compositeIndex, comptype, compIns, Instance, portName,action) \
SubComponent_i_smt(compositeName,compositeIndex,comptype,compIns, Instance, _PORT_##portName##_##ACTION_##action##_REFINED_ICONTRACT_RESPONSE)

#define refined_contract2(compositeName, compositeIndex, comptype, compIns,Instance, portName,action) \
SubComponent_i_smt(compositeName,compositeIndex,comptype,compIns, Instance, _PORT_##portName##_##ACTION_##action##_REFINED_ICONTRACT)

#define refined_contract_reject(compositeName, compositeIndex, comptype, compIns,Instance, portName,action) \
SubComponent_i_smt(compositeName,compositeIndex,comptype,compIns, Instance, _PORT_##portName##_##ACTION_##action##_REFINED_ICONTRACT_REJECT)

#define port_index(CompositeName, CompositeID,compType,CompInsID,Instance, portName, portID) \
 c_expr{[ValueOfProcessLocalVar(CompositeName, CompositeID, compType, CompInsID,Instance, portID)]};

//macro for returning the port size array
#define Component_i_port_j_Size(compType, Instance, portName)  Component_i_smt(compType,Instance,portName##_SIZE)

/******Macros for Component Instance Specification********/

//COMPONENT PARAMETER
////Macro for instance PARAMETER
#define Component_i_Param_N(compositeName,compositeID,component,compIns,Instance,N)  \
SubComponent_i_smt(compositeName,compositeID,component,compIns,Instance,_PARAM_##N)

#define Connector_i_Param_N(compositeName,compositeID,connector,connIns_i,N)  \
SubConnector_i_smt(compositeName,compositeID,connector,connIns_i,_PARAM_##N)


//COMPONENT PORT
////Macro for number of instances for a PORT
#define Component_i_Port_Num(compositeName, compositeIndex, component,compIns,Instance, pname) \
            SubComponent_i_smt(compositeName,compositeIndex,component,compIns,Instance,_PORT_##pname##_NUM)
////Macro for number of connected PORTS to CONSUMER/PROVIDED PORT instance
#define Component_i_port_j_CONNECTIONS(compositeName,compositeIndex,component,compIns,Instance,j, port) Component_i_port_j_CONNECTIONS_2(compositeName,compositeIndex,component,compIns,Instance,j, port)
#define Component_i_port_j_CONNECTIONS_2(compositeName, compositeIndex, component,compIns,Instance,j, port) \
SubComponent_i_smt(compositeName,compositeIndex,component,compIns,Instance,_PORT_##port##_##j##_CONNECTIONS)
////Macro for number of connected PORTS to ALL instances of a CONSUMER/PROVIDED PORT
#define Component_i_port_SUMCONNECTIONS(compositeName, compositeIndex, component, compIns,Instance,port) \
SubComponent_i_smt(compositeName,compositeIndex,component,compIns,Instance,_PORT_##port##_SUMCONNECTIONS)
//Macro for number of connected PORTS to CONSUMER/PROVIDED PORT instance
#define Component_i_port_NUMOFCONNECTIONS(compositeName, compositeIndex, component, compIns,Instance,port) \
SubComponent_i_smt(compositeName,compositeIndex,component,compIns,Instance,_PORT_##port##_NUMOFCONNECTIONS)
////macro for a name of the array that holds the offset of the CONSUMER/PROVIDED PORTS
#define Component_i_port_offsetArray(compositeName,compositeIndex, component,compIns, i, pname) \
   SubComponent_i_smt(compositeName,compositeIndex, component,compIns, i,_PORT_##pname##_OFFSET)

////macro for buffer of the CONSUMER/PROVIDED PORTS
#define Component_i_port_buffer(component, Instance, port) PORT_##port##_BUFFER
////macro for a name of the array that holds the number of CONNECTIONS for the instances of a PORT
#define Component_i_port_connectionArray(component, Instance, pname) Component_i_smt(component,Instance,_PORT_##pname##_CONNECTS)
////macro for an array slot that stores the number of CONNECTIONS for a PORT INSTANCE
#define port_connection_size(CompositeName, CompositeID,Component, CompInsID,Instance, pname, pnum)\
               c_expr{Component_i_port_connectionArray(Component, Instance, pname)[ValueOfProcessLocalVar(CompositeName, CompositeID, Component,CompInsID, Instance, pnum)]}
////macro for an array index that stores the BUFFER value received from a specific CONNECTION of a PORT INSTANCE
#define slot(CompositeName, CompositeID,Component,CompInsID, Instance, pname, pnum, conn) \
          c_expr{Component_i_port_offsetArray(CompositeName, CompositeID,Component,CompInsID, Instance, pname)[ValueOfProcessLocalVar(CompositeName, CompositeID, Component, CompInsID,Instance, pnum)]} + conn
#define slot2(CompositeName, CompositeID,Component, CompInsID, Instance, pname, pnum, conn) \
          c_expr{Component_i_port_offsetArray(CompositeName, CompositeID,Component,CompInsID, Instance, pname)[ValueOfProcessLocalVar(CompositeName, CompositeID, Component, CompInsID,Instance, pnum)]}%1 + conn
////macro for returning the statement that enable the use of a process variable in a c_expr
#define ValueOfProcessLocalVar(CompositeName, CompositeID, Component, CompInstance, Instance, var) \
       PCOMPONENT_TYPE_##CompositeName##_##CompositeID##_SUBCOMPONENT_## Component##_##CompInstance##_##Instance-> var

////macro for the name of the array which stores the INDEX of a CHANNEL that the INSTANCES of a PORT uses
#define Component_i_portChannelIndexArray(compType, Instance, pname) Component_i_smt(component,Instance,_PORT_##pname##_CHINDEXARRAY)
////macros for the INDEX of a CHANNEL that an INSTANCE of a PORT uses
#define Component_i_portChannelIndex(CompositeName, CompositeID,compType, Instance, pname, pnum)\
              c_expr{Component_i_portChannelIndexArray(compType, Instance, pname)[ValueOfProcessLocalVar(CompositeName, CompositeID, compType, Instance, pnum)]}
#define Component_i_portChannelIndex_CONS(compType, Instance, pname, consVal) \
             c_expr{Component_i_portChannelIndexArray(compType, Instance, pname)[consVal]}
////macro for the INDEX value of a CHANNEL that a COMPONENT PORT uses
#define Component_i_channelIndexValue(compType, cInstance, pname, pInstance) \
                       Component_i_smt(compType,cInstance,_PORT_##pname##_##pInstance##_CHINDEX)

////macros for the name of the CHANNELS that a COMPONENT PORT uses
#define Component_i_channelName(compositeName, compositeIndex,compType, compIns, cInstance, pname) \
        Component_i_smt_CH_ins(compositeName, compositeIndex, CHANNEL_,compType,compIns,cInstance,_PORT_##pname)

#define Component_i_channelName_action(compositeName, compositeIndex,compType,compInstanceID, cInstance, pname,action) \
 Component_i_smt_CH_ins(compositeName, compositeIndex, CHANNEL_,compType,compInstanceID,cInstance,_PORT_##pname##_ACTION_##action)

#define Component_i_channelName_action_req(compositeName, compositeIndex,compType,compInstanceID, cInstance, pname,action) \
 Component_i_smt_CH_ins(compositeName, compositeIndex, CHANNELREQ_,compType,compInstanceID,cInstance,_PORT_##pname##_ACTION_##action)

#define Component_i_channelName_action_res(compositeName, compositeIndex, compType,compInstanceID, cInstance, pname,action)\
 Component_i_smt_CH_ins(compositeName, compositeIndex, CHANNELRES_,compType,compInstanceID,cInstance,_PORT_##pname##_ACTION_##action)

#define Component_i_channelName_req(compositeName, compositeIndex, compType, compInstanceID, cInstance, pname) \
             Component_i_smt_CH_ins(compositeName, compositeIndex,CHANNELREQ_,compType,compInstanceID,cInstance,_PORT_##pname)

#define Component_i_channelName_res(compositeName, compositeIndex, compType,compInstanceID, cInstance, pname) \
             Component_i_smt_CH_ins(compositeName, compositeIndex,CHANNELRES_,compType,compInstanceID,cInstance,_PORT_##pname)


#define Component_i_smt_CH_ins(compositeName, compositeIndex,chpre, compType,compInstanceID, i,smt) \
                Component_i_2_smt_CH_ins(compositeName, compositeIndex,chpre, compType,compInstanceID,i, smt)
#define Component_i_2_smt_CH_ins(compositeName, compositeIndex,chpre,component,compInstanceID,i,smt) \
                 chpre##compositeName##_##compositeIndex##_##COMPONENT_##component##_##compInstanceID##_## i ## smt


#define Component_i_smt_CH(compositeName, compositeIndex,chpre, compType, i,smt) \
                Component_i_2_smt_CH(compositeName, compositeIndex,chpre, compType,i, smt)
#define Component_i_2_smt_CH(compositeName, compositeIndex,chpre,component,i,smt) \
                 chpre##compositeName##_##compositeIndex##_##COMPONENT_##component##_## i ## smt

//macro for the name of c_code block
#define Component_i_c_code(compositeName, compositeIndex,compType, compIns,Instance) SubComponent_i_smt(compositeName,compositeIndex,compType,compIns,Instance,_ARRAY_INITIALISATIONS)()
//macro for the name of the role data block
#define Component_i_roleData(compType, compInstance,Instance) Component_i_smt_ins(compType,compInstance, Instance,_ROLE_DATA)()

#define component_i_blockedChannel(compositeName, compositeID, compType, compInstance, Instance)\
SubComponent_i_smt(compositeName,compositeID,compType,compInstance,Instance,_BLOCKEDCHANNEL)

#define component_i_role_dataUpdate(compositeName, compositeID, compType,compIns, Instance, port,action) \
SubComponent_i_smt(compositeName,compositeID,compType,compIns,Instance,_PORT_##port##_ACTION_##action##_ROLE_DATAUPDATE)()

#define component_i_role_dataUpdate_request(compositeName, compositeID, compType,compIns, Instance, port,action) \
SubComponent_i_smt(compositeName,compositeID,compType,compIns,Instance,_PORT_##port##_ACTION_##action##_ROLE_DATAUPDATE_REQUEST)()

#define component_i_role_dataUpdate_response(compositeName, compositeID, compType,compIns, Instance, port,action) \
SubComponent_i_smt(compositeName,compositeID,compType,compIns,Instance,_PORT_##port##_ACTION_##action##_ROLE_DATAUPDATE_RESPONSE)()

#endif
