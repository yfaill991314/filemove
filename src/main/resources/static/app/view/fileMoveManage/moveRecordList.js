Ext.define('app.view.fileMoveManage.moveRecordList', {
    extend: 'Ext.grid.Panel',
    xtype: 'view-fileMoveManage-moveRecordList',
    requires: [],
    padding: "10px 20px 0 20px",
    config: {
        tabtitle: ''
    },
    constructor: function (config) {
        var me = this;
        config = config || {};
        Ext.applyIf(config, me.config);
        this.callParent(arguments);
    },
    initComponent: function () {
        var me = this;
        var newstore = Ext.create('Ext.data.Store', {
            id: 'simpsonsStore',
            autoLoad: true,
            pageSize: 20, // 每页的条目数量
            proxy: {
                type: 'ajax',
                url: 'fileMoveRecord/fileMoveRecordList',
                reader: {
                    type: 'json',
                    rootProperty: 'rows',
                    totalProperty: "total"
                }
            }
        });
        Ext.apply(me, {
            tbar: {
                layout: 'column',
                scope: me,
                items: [
                    {
                        xtype: 'toolbar',
                        columnWidth: 1,
                        scope: me,
                        itemId: 'search',
                        items: [
                            {
                                xtype: 'button',
                                text: '手动重迁',
                                scope: me,
                                itemId: 'bt_moveagain',
                                handler: function () {
                                    //判断是否选择额一条数据。
                                    var selectData = me.getSelectionModel().getSelection();
                                    if (selectData.length != 1) {
                                        Ext.Msg.alert('温馨提示', '请选择一条操作数据');
                                        return;
                                    }
                                    // if(selectData[0].get('moveStatus')!='迁移失败'){
                                    //     Ext.Msg.alert('温馨提示', '手动重迁仅适用于迁移失败状态的任务');
                                    //     return;
                                    // }
                                    me.remigrate(selectData[0]);
                                }
                            },
                            {
                                xtype: 'button',
                                text: '导入任务表',
                                scope: me,
                                itemId: 'bt_importTaskTable',
                                handler: function () {
                                    var dataSource = me.queryById("dataSourceId").getValue();
                                    var tableName = me.queryById("tableNameId").getValue();
                                    var params = {};
                                    if (dataSource!=null && dataSource!=''){
                                        params.dataSource = dataSource;
                                    }
                                    if (tableName!=null && tableName!=''){
                                        params.tableName = tableName;
                                    }
                                    me.importTaskTable(params);
                                }
                            },
                            {
                                xtype: 'button',
                                text: '清除数据',
                                scope: me,
                                itemId: 'bt_clearData',
                                handler: function () {
                                    var dataSource = me.queryById("dataSourceId").getValue();
                                    var tableName = me.queryById("tableNameId").getValue();
                                    var params = {};
                                    if (dataSource!=null && dataSource!=''){
                                        params.dataSource = dataSource;
                                    }
                                    if (tableName!=null && tableName!=''){
                                        params.tableName = tableName;
                                    }
                                    me.clearData(params);
                                }
                            },
                            {
                                xtype: 'button',
                                text: '定时任务状态',
                                scope: me,
                                itemId: 'bt_taskStatus',
                                handler: function () {
                                    me.selectFileMoveTaskStatus();
                                }
                            },
                            {
                                xtype: 'button',
                                text: '终止当前迁移',
                                scope: me,
                                itemId: 'bt_stopmoveId',
                                handler: function () {
                                    me.stopmove();
                                }
                            },
                            '->',
                            {
                                xtype: 'combobox',
                                emptyText: '数据源',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '中心城区', 'value': '510100'},
                                        {'name': '天府新区', 'value': '510142'},
                                        {'name': '龙泉驿', 'value': '510112'},
                                        {'name': '双流', 'value': '510122'},
                                        {'name': '青白江', 'value': '510113'},
                                        {'name': '温江', 'value': '510115'},
                                        {'name': '郫县', 'value': '510124'},
                                        {'name': '新都', 'value': '510114'},
                                        {'name': '简阳', 'value': '510180'},
                                        {'name': '金堂', 'value': '510121'},
                                        {'name': '大邑', 'value': '510129'},
                                        {'name': '浦江', 'value': '510131'},
                                        {'name': '新津', 'value': '510132'},
                                        {'name': '都江堰', 'value': '510181'},
                                        {'name': '彭州', 'value': '510182'},
                                        {'name': '邛崃', 'value': '510183'},
                                        {'name': '崇州', 'value': '510184'}
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 100,
                                itemId: 'dataSourceId'
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '数据表',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': 'mgmapfigure', 'value': 'mgmapfigure'},
                                        {'name': 'mgdoorimg', 'value': 'mgdoorimg'},
                                        {'name': 'imgimages', 'value': 'imgimages'}
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 100,
                                itemId: 'tableNameId'
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '任务状态',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '未迁移', 'value': '未迁移'},
                                        {'name': '迁移成功', 'value': '迁移成功'},
                                        {'name': '迁移失败', 'value': '迁移失败'},
                                        {'name': '不迁移', 'value': '不迁移'},
                                        {'name': '已入中心库', 'value': '已入中心库'}
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 100,
                                itemId: 'moveStatusId'
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '查询条件',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '任务uuid', 'value': 'uuid'},
                                        {'name': '文件uuid', 'value': 'fileUuid'},
                                        {'name': '被迁移文件源id', 'value': 'bizid'},
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 100,
                                itemId: 'searchCom'
                            },
                            {
                                xtype: 'textfield',
                                emptyText: '请输入',
                                labelWidth: 60,
                                width: 100,
                                itemId: 'searchContent'
                            },
                            {
                                xtype: 'button', text: '搜索', scope: me,
                                handler: function () {
                                    var dataSource = me.queryById("dataSourceId").getValue();
                                    var tableName = me.queryById("tableNameId").getValue();
                                    var moveStatus = me.queryById("moveStatusId").getValue();
                                    var searchCom = me.queryById("searchCom").getValue();
                                    var searchContent = me.queryById("searchContent").getValue();

                                    var params = {};
                                    if (searchCom!=null && searchCom!=''
                                    && searchContent!=null && searchContent!=''){
                                        params[searchCom] = searchContent;
                                    }
                                    if (dataSource!=null && dataSource!=''){
                                        params.dataSource = dataSource;
                                    }
                                    if (tableName!=null && tableName!=''){
                                        params.tableName = tableName;
                                    }
                                    if(moveStatus!=null && moveStatus!=''){
                                        params.moveStatus = moveStatus;
                                    }

                                    me.store.getProxy().extraParams=params;
                                    me.store.load();
                                }
                            },
                            {
                                xtype: 'button', text: '重置', scope: me,
                                handler: function () {
                                    me.queryById("searchCom").setValue(null);
                                    me.queryById("searchContent").setValue(null);
                                    me.queryById("dataSourceId").setValue(null);
                                    me.queryById("tableNameId").setValue(null);
                                    me.queryById("moveStatusId").setValue(null);
                                }
                            }
                        ]
                    }
                ]
            },
            border: true,
            store: newstore,
            title: me.config.tabtitle,
            // columnLines: true,
            // closable: true,
            viewConfig: {
                enableTextSelection: true
            },
            columns: [
                {text: '任务uuid', dataIndex: 'uuid', width: '9%', align: 'center'},
                {text: '业务id（bizid）', dataIndex: 'bizid', width: '9%', align: 'center'},
                {text: '状态', dataIndex: 'moveStatus', width: '9%', align: 'center'},
                {text: '备注', dataIndex: 'remark', width: '9%', align: 'center'},
                {text: '迁移线程', dataIndex: 'threadId', width: '9%', align: 'center'},
                {text: '文件uuid', dataIndex: 'fileUuid', width: '10%', align: 'center'},
                {text: 'storeid', dataIndex: 'fileStoreId', width: '9%', align: 'center'},
                {text: '文件大小/byte', dataIndex: 'fileSize', width: '9%', align: 'center'},
                {text: '迁移时间', dataIndex: 'createtime', width: '9%', align: 'center'},
                {text: '数据源', dataIndex: 'dataSource', width: '9%', align: 'center'},
                {text: '数据来源表', dataIndex: 'tablename', width: '9%', align: 'center'},
            ],
            listeners: {
            },
            dockedItems: [
                {
                    xtype: 'pagingtoolbar',
                    dock: 'bottom',
                    displayInfo: true,
                    store: newstore
                }
            ]


        })
        ;
        me.callParent(arguments);
    },


    remigrate:function(selectData){
        var me = this;
        var myMask = new Ext.LoadMask(me,{msg:"请稍等,操作正在进行..."});
        myMask.show();
        Ext.Ajax.request({
            url: 'fileMoveRecord/remigrate',
            params: {'uuid': selectData.get('uuid'),'tableName': selectData.get('tablename'),'dataSource': selectData.get('dataSource')},
            method: 'POST',
            // async: false,
            success: function (response, options) {
                myMask.hide();
                var response = JSON.parse(response.responseText);
                if (response.code==200) {
                    me.store.reload();
                } else {
                    Ext.Msg.alert("系统提示", response.message);
                }
            },
            failure: function (response) {
                myMask.hide();
                Ext.Msg.alert("系统提示", "请求超时");
            }
        });
    },
    importTaskTable: function (params) {
        var me = this;
        Ext.Msg.alert('提示','任务导入已开始');
        Ext.Ajax.request({
            url: 'fileMoveRecord/importTaskTable',
            params: params,
            method: 'POST',
            timeout: 0,
            success: function (response, options) {
                var response = JSON.parse(response.responseText);
                if (response.code==200) {
                    Ext.Msg.alert("系统提示", response.message);
                    me.store.reload();
                } else {
                    Ext.Msg.alert("系统提示", response.message);
                }
            },
            failure: function (response) {
                Ext.Msg.alert("系统提示", "请求超时");
            }
        });
    },
    clearData: function (params) {
        var me = this;
        var myMask = new Ext.LoadMask(me,{msg:"请稍等,操作正在进行..."});
        myMask.show();
        Ext.Ajax.request({
            url: 'fileMoveRecord/clearData',
            params: params,
            method: 'POST',
            // async: false,
            timeout: 600000,
            success: function (response, options) {
                myMask.hide();
                var response = JSON.parse(response.responseText);
                if (response.code==200) {
                    Ext.Msg.alert("系统提示", response.message);
                    me.store.reload();
                } else {
                    Ext.Msg.alert("系统提示", response.message);
                }
            },
            failure: function (response) {
                myMask.hide();
                Ext.Msg.alert("系统提示", "请求超时");
            }
        });
    },
    selectFileMoveTaskStatus:function () {
        var me = this;
        var myMask = new Ext.LoadMask(me,{msg:"请稍等,操作正在进行..."});
        myMask.show();
        Ext.Ajax.request({
            url: 'fileMoveRecord/selectFileMoveTaskStatus',
            method: 'POST',
            async: false,
            success: function (response, options) {
                myMask.hide();
                var response = JSON.parse(response.responseText);
                if (response.code==200) {
                    Ext.create("Ext.window.Window", {
                        title: "定时任务状态",
                        modal: true,
                        layout: 'fit',
                        width: '40%',
                        height: '40%',
                        scrollable: 'y',
                        resizable: false,
                        padding: '10 10 10 10',
                        items:[
                            {
                                layout: 'form',
                                items:[{
                                    xtype: 'combobox',
                                    emptyText: '请选择',
                                    store: {
                                        fields: ['name'],
                                        autoLoad: true,
                                        data: [
                                            {'name': '关闭', 'value': '关闭'},
                                            {'name': '开启', 'value': '开启'},
                                        ]
                                    },
                                    fieldLabel:'定时任务状态',
                                    editable: false,
                                    displayField: 'name',
                                    valueField: 'value',
                                    labelWidth: 60,
                                    value:response.data.taskStatus,
                                    width: 200,
                                    itemId: 'taskStatusId'
                                },]
                            }
                        ],
                        fbar: [
                            '->',
                            {
                                xtype: 'button',
                                text: "保存",
                                handler: function (btn) {
                                    var params={};
                                    var win = btn.up().up();
                                    params.taskStatus=win.queryById('taskStatusId').getValue();
                                    Ext.Ajax.request({
                                        url: 'fileMoveRecord/updateFileMoveTaskStatus',
                                        params: params,
                                        method: 'POST',
                                        async: false,
                                        success: function (response, options) {
                                            var response = JSON.parse(response.responseText);
                                            if (response.code==200) {
                                                Ext.Msg.alert("系统提示", response.message);
                                                var win = btn.up().up();
                                                win.close();
                                            } else {
                                                Ext.Msg.alert("系统提示", response.message);
                                            }
                                        },
                                        failure: function (response) {
                                            Ext.Msg.alert("系统提示", "请求超时");
                                        }
                                    });
                                }
                            },
                            {
                                xtype: 'button',
                                text: "关闭",
                                handler: function (btn) {
                                    var win = btn.up().up();
                                    win.close();
                                }
                            }
                        ]
                    }).show();
                } else {
                    Ext.Msg.alert("系统提示", response.message);
                }
            },
            failure: function (response) {
                myMask.hide();
                Ext.Msg.alert("系统提示", "请求超时");
            }
        });
    },
    stopmove:function () {
        var me = this;
        var myMask = new Ext.LoadMask(me,{msg:"请稍等,操作正在进行..."});
        myMask.show();
        Ext.Ajax.request({
            url: 'fileMoveRecord/stopMove',
            method: 'POST',
            async: false,
            success: function (response, options) {
                myMask.hide();
                var response = JSON.parse(response.responseText);
                if (response.code==200) {
                    Ext.Msg.alert("系统提示", response.message);
                } else {
                    Ext.Msg.alert("系统提示", response.message);
                }
            },
            failure: function (response) {
                myMask.hide();
                Ext.Msg.alert("系统提示", "请求超时");
            }
        });
    }
});
