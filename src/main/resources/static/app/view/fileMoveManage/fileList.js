Ext.define('app.view.fileMoveManage.fileList', {
    extend: 'Ext.grid.Panel',
    xtype: 'view-fileMg-fileList',
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
                url: 'fastFile/findFileList',
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
                                text: '下载',
                                scope: me,
                                itemId: 'bt_downLoad',
                                handler: function () {
                                    //判断是否选择额一条数据。
                                    var selectData = me.getSelectionModel().getSelection();
                                    if (selectData.length != 1) {
                                        Ext.Msg.alert('温馨提示', '请选择一条操作数据');
                                        return;
                                    }
                                    var fileUuid = selectData[0].get('uuid');
                                    window.open('/filemove/fastFile/download?fileUuid=' + fileUuid );
                                }
                            },
                            '->',
                            {
                                xtype: 'combobox',
                                emptyText: '请选择',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '文件uuid', 'value': 'uuid'},
                                        {'name': '被绑定业务件uuid', 'value': 'businessUuid'},
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 200,
                                itemId: 'searchCom'
                            },
                            {
                                xtype: 'textfield',
                                emptyText: '请输入',
                                labelWidth: 60,
                                width: 220,
                                itemId: 'searchContent'
                            },
                            {
                                xtype: 'button', text: '搜索', scope: me, glyph: 'xf002@FontAwesome',
                                handler: function () {
                                    var searchCom = me.queryById("searchCom").getValue();
                                    var searchContent = me.queryById("searchContent").getValue();

                                    var params = {};
                                    params[searchCom] = searchContent;
                                    me.store.getProxy().extraParams=params;
                                    me.store.load();
                                }
                            },
                            {
                                xtype: 'button', text: '重置', scope: me, glyph: 'xf0e2@FontAwesome',
                                handler: function () {
                                    me.queryById("searchCom").setValue(null);
                                    me.queryById("searchContent").setValue(null);
                                }
                            }
                        ]
                    }
                ]
            },
            border: true,
            store: newstore,
            title: me.config.tabtitle,
            viewConfig: {
                enableTextSelection: true
            },
            columns: [
                {text: '文件uuid', dataIndex: 'uuid', width: '15%', align: 'center'},
                {text: '文件名称', dataIndex: 'fileName', width: '15%', align: 'center'},
                {text: '所属系统', dataIndex: 'systemCode', width: '10%', align: 'center'},
                {text: '上传时间', dataIndex: 'createTime', width: '15%', align: 'center'},
                {text: '上传人id', dataIndex: 'creatorId', width: '10%', align: 'center'},
                {text: '大小/byte', dataIndex: 'fileSize', width: '10%', align: 'center'},
                {text: 'storeid', dataIndex: 'fileStoreId', width: '15%', align: 'center'},
                {text: '状态', dataIndex: 'status', width: '10%', align: 'center'}
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
    }
});
