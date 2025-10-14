/****************************************************************************
** Meta object code from reading C++ file 'mqttmanager.h'
**
** Created by: The Qt Meta Object Compiler version 68 (Qt 6.4.2)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include <memory>
#include "../../../mqttmanager.h"
#include <QtCore/qmetatype.h>
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'mqttmanager.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 68
#error "This file was generated using the moc from 6.4.2. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

#ifndef Q_CONSTINIT
#define Q_CONSTINIT
#endif

QT_BEGIN_MOC_NAMESPACE
QT_WARNING_PUSH
QT_WARNING_DISABLE_DEPRECATED
namespace {
struct qt_meta_stringdata_MqttManager_t {
    uint offsetsAndSizes[36];
    char stringdata0[12];
    char stringdata1[11];
    char stringdata2[1];
    char stringdata3[18];
    char stringdata4[19];
    char stringdata5[16];
    char stringdata6[21];
    char stringdata7[13];
    char stringdata8[18];
    char stringdata9[8];
    char stringdata10[15];
    char stringdata11[6];
    char stringdata12[4];
    char stringdata13[11];
    char stringdata14[12];
    char stringdata15[9];
    char stringdata16[14];
    char stringdata17[6];
};
#define QT_MOC_LITERAL(ofs, len) \
    uint(sizeof(qt_meta_stringdata_MqttManager_t::offsetsAndSizes) + ofs), len 
Q_CONSTINIT static const qt_meta_stringdata_MqttManager_t qt_meta_stringdata_MqttManager = {
    {
        QT_MOC_LITERAL(0, 11),  // "MqttManager"
        QT_MOC_LITERAL(12, 10),  // "etaChanged"
        QT_MOC_LITERAL(23, 0),  // ""
        QT_MOC_LITERAL(24, 17),  // "totalLanesChanged"
        QT_MOC_LITERAL(42, 18),  // "currentLaneChanged"
        QT_MOC_LITERAL(61, 15),  // "avoidDirChanged"
        QT_MOC_LITERAL(77, 20),  // "ambulanceLaneChanged"
        QT_MOC_LITERAL(98, 12),  // "stateChanged"
        QT_MOC_LITERAL(111, 17),  // "onMessageReceived"
        QT_MOC_LITERAL(129, 7),  // "message"
        QT_MOC_LITERAL(137, 14),  // "QMqttTopicName"
        QT_MOC_LITERAL(152, 5),  // "topic"
        QT_MOC_LITERAL(158, 3),  // "eta"
        QT_MOC_LITERAL(162, 10),  // "totalLanes"
        QT_MOC_LITERAL(173, 11),  // "currentLane"
        QT_MOC_LITERAL(185, 8),  // "avoidDir"
        QT_MOC_LITERAL(194, 13),  // "ambulanceLane"
        QT_MOC_LITERAL(208, 5)   // "state"
    },
    "MqttManager",
    "etaChanged",
    "",
    "totalLanesChanged",
    "currentLaneChanged",
    "avoidDirChanged",
    "ambulanceLaneChanged",
    "stateChanged",
    "onMessageReceived",
    "message",
    "QMqttTopicName",
    "topic",
    "eta",
    "totalLanes",
    "currentLane",
    "avoidDir",
    "ambulanceLane",
    "state"
};
#undef QT_MOC_LITERAL
} // unnamed namespace

Q_CONSTINIT static const uint qt_meta_data_MqttManager[] = {

 // content:
      10,       // revision
       0,       // classname
       0,    0, // classinfo
       7,   14, // methods
       6,   67, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       6,       // signalCount

 // signals: name, argc, parameters, tag, flags, initial metatype offsets
       1,    0,   56,    2, 0x06,    7 /* Public */,
       3,    0,   57,    2, 0x06,    8 /* Public */,
       4,    0,   58,    2, 0x06,    9 /* Public */,
       5,    0,   59,    2, 0x06,   10 /* Public */,
       6,    0,   60,    2, 0x06,   11 /* Public */,
       7,    0,   61,    2, 0x06,   12 /* Public */,

 // slots: name, argc, parameters, tag, flags, initial metatype offsets
       8,    2,   62,    2, 0x08,   13 /* Private */,

 // signals: parameters
    QMetaType::Void,
    QMetaType::Void,
    QMetaType::Void,
    QMetaType::Void,
    QMetaType::Void,
    QMetaType::Void,

 // slots: parameters
    QMetaType::Void, QMetaType::QByteArray, 0x80000000 | 10,    9,   11,

 // properties: name, type, flags
      12, QMetaType::QString, 0x00015001, uint(0), 0,
      13, QMetaType::Int, 0x00015001, uint(1), 0,
      14, QMetaType::Int, 0x00015001, uint(2), 0,
      15, QMetaType::Int, 0x00015001, uint(3), 0,
      16, QMetaType::Int, 0x00015001, uint(4), 0,
      17, QMetaType::QString, 0x00015001, uint(5), 0,

       0        // eod
};

Q_CONSTINIT const QMetaObject MqttManager::staticMetaObject = { {
    QMetaObject::SuperData::link<QObject::staticMetaObject>(),
    qt_meta_stringdata_MqttManager.offsetsAndSizes,
    qt_meta_data_MqttManager,
    qt_static_metacall,
    nullptr,
    qt_incomplete_metaTypeArray<qt_meta_stringdata_MqttManager_t,
        // property 'eta'
        QtPrivate::TypeAndForceComplete<QString, std::true_type>,
        // property 'totalLanes'
        QtPrivate::TypeAndForceComplete<int, std::true_type>,
        // property 'currentLane'
        QtPrivate::TypeAndForceComplete<int, std::true_type>,
        // property 'avoidDir'
        QtPrivate::TypeAndForceComplete<int, std::true_type>,
        // property 'ambulanceLane'
        QtPrivate::TypeAndForceComplete<int, std::true_type>,
        // property 'state'
        QtPrivate::TypeAndForceComplete<QString, std::true_type>,
        // Q_OBJECT / Q_GADGET
        QtPrivate::TypeAndForceComplete<MqttManager, std::true_type>,
        // method 'etaChanged'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        // method 'totalLanesChanged'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        // method 'currentLaneChanged'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        // method 'avoidDirChanged'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        // method 'ambulanceLaneChanged'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        // method 'stateChanged'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        // method 'onMessageReceived'
        QtPrivate::TypeAndForceComplete<void, std::false_type>,
        QtPrivate::TypeAndForceComplete<const QByteArray &, std::false_type>,
        QtPrivate::TypeAndForceComplete<const QMqttTopicName &, std::false_type>
    >,
    nullptr
} };

void MqttManager::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        auto *_t = static_cast<MqttManager *>(_o);
        (void)_t;
        switch (_id) {
        case 0: _t->etaChanged(); break;
        case 1: _t->totalLanesChanged(); break;
        case 2: _t->currentLaneChanged(); break;
        case 3: _t->avoidDirChanged(); break;
        case 4: _t->ambulanceLaneChanged(); break;
        case 5: _t->stateChanged(); break;
        case 6: _t->onMessageReceived((*reinterpret_cast< std::add_pointer_t<QByteArray>>(_a[1])),(*reinterpret_cast< std::add_pointer_t<QMqttTopicName>>(_a[2]))); break;
        default: ;
        }
    } else if (_c == QMetaObject::RegisterMethodArgumentMetaType) {
        switch (_id) {
        default: *reinterpret_cast<QMetaType *>(_a[0]) = QMetaType(); break;
        case 6:
            switch (*reinterpret_cast<int*>(_a[1])) {
            default: *reinterpret_cast<QMetaType *>(_a[0]) = QMetaType(); break;
            case 1:
                *reinterpret_cast<QMetaType *>(_a[0]) = QMetaType::fromType< QMqttTopicName >(); break;
            }
            break;
        }
    } else if (_c == QMetaObject::IndexOfMethod) {
        int *result = reinterpret_cast<int *>(_a[0]);
        {
            using _t = void (MqttManager::*)();
            if (_t _q_method = &MqttManager::etaChanged; *reinterpret_cast<_t *>(_a[1]) == _q_method) {
                *result = 0;
                return;
            }
        }
        {
            using _t = void (MqttManager::*)();
            if (_t _q_method = &MqttManager::totalLanesChanged; *reinterpret_cast<_t *>(_a[1]) == _q_method) {
                *result = 1;
                return;
            }
        }
        {
            using _t = void (MqttManager::*)();
            if (_t _q_method = &MqttManager::currentLaneChanged; *reinterpret_cast<_t *>(_a[1]) == _q_method) {
                *result = 2;
                return;
            }
        }
        {
            using _t = void (MqttManager::*)();
            if (_t _q_method = &MqttManager::avoidDirChanged; *reinterpret_cast<_t *>(_a[1]) == _q_method) {
                *result = 3;
                return;
            }
        }
        {
            using _t = void (MqttManager::*)();
            if (_t _q_method = &MqttManager::ambulanceLaneChanged; *reinterpret_cast<_t *>(_a[1]) == _q_method) {
                *result = 4;
                return;
            }
        }
        {
            using _t = void (MqttManager::*)();
            if (_t _q_method = &MqttManager::stateChanged; *reinterpret_cast<_t *>(_a[1]) == _q_method) {
                *result = 5;
                return;
            }
        }
    }else if (_c == QMetaObject::ReadProperty) {
        auto *_t = static_cast<MqttManager *>(_o);
        (void)_t;
        void *_v = _a[0];
        switch (_id) {
        case 0: *reinterpret_cast< QString*>(_v) = _t->eta(); break;
        case 1: *reinterpret_cast< int*>(_v) = _t->totalLanes(); break;
        case 2: *reinterpret_cast< int*>(_v) = _t->currentLane(); break;
        case 3: *reinterpret_cast< int*>(_v) = _t->avoidDir(); break;
        case 4: *reinterpret_cast< int*>(_v) = _t->ambulanceLane(); break;
        case 5: *reinterpret_cast< QString*>(_v) = _t->state(); break;
        default: break;
        }
    } else if (_c == QMetaObject::WriteProperty) {
    } else if (_c == QMetaObject::ResetProperty) {
    } else if (_c == QMetaObject::BindableProperty) {
    }
}

const QMetaObject *MqttManager::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->dynamicMetaObject() : &staticMetaObject;
}

void *MqttManager::qt_metacast(const char *_clname)
{
    if (!_clname) return nullptr;
    if (!strcmp(_clname, qt_meta_stringdata_MqttManager.stringdata0))
        return static_cast<void*>(this);
    return QObject::qt_metacast(_clname);
}

int MqttManager::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 7)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 7;
    } else if (_c == QMetaObject::RegisterMethodArgumentMetaType) {
        if (_id < 7)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 7;
    }else if (_c == QMetaObject::ReadProperty || _c == QMetaObject::WriteProperty
            || _c == QMetaObject::ResetProperty || _c == QMetaObject::BindableProperty
            || _c == QMetaObject::RegisterPropertyMetaType) {
        qt_static_metacall(this, _c, _id, _a);
        _id -= 6;
    }
    return _id;
}

// SIGNAL 0
void MqttManager::etaChanged()
{
    QMetaObject::activate(this, &staticMetaObject, 0, nullptr);
}

// SIGNAL 1
void MqttManager::totalLanesChanged()
{
    QMetaObject::activate(this, &staticMetaObject, 1, nullptr);
}

// SIGNAL 2
void MqttManager::currentLaneChanged()
{
    QMetaObject::activate(this, &staticMetaObject, 2, nullptr);
}

// SIGNAL 3
void MqttManager::avoidDirChanged()
{
    QMetaObject::activate(this, &staticMetaObject, 3, nullptr);
}

// SIGNAL 4
void MqttManager::ambulanceLaneChanged()
{
    QMetaObject::activate(this, &staticMetaObject, 4, nullptr);
}

// SIGNAL 5
void MqttManager::stateChanged()
{
    QMetaObject::activate(this, &staticMetaObject, 5, nullptr);
}
QT_WARNING_POP
QT_END_MOC_NAMESPACE
